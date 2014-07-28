import java.io._
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler._
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

def changeHandler(handler: org.eclipse.jetty.server.handler.AbstractHandler, actualHandler:ContextHandler, server: Server) = {
  server.stop
  actualHandler.setHandler(handler)
  server.start
}

def getServer(sc: org.apache.spark.SparkContext):Server = {
  def allow(field: java.lang.reflect.Field)=field.setAccessible(true)
  val field = sc.getClass.getDeclaredField("ui");
  allow(field)
  val ui = field.get(sc)
  val serverInfoField = Class.forName("org.apache.spark.ui.WebUI").getDeclaredField("serverInfo")
  allow(serverInfoField)
  val infoOption:Object = serverInfoField.get(ui)
  val optionMethod = Class.forName("scala.Option").getDeclaredMethod("get")
  optionMethod.setAccessible(true)
  val serverInfo:Object = optionMethod.invoke(infoOption)
  val serverField = Class.forName("org.apache.spark.ui.ServerInfo").getDeclaredField("server")
  allow(serverField)
  return serverField.get(serverInfo).asInstanceOf[org.eclipse.jetty.server.Server]
}


def addPluginHandler(server:Server):ContextHandler = {
  val pluginHandler = new ContextHandler("/plugins")
  pluginHandler.setHandler(new DefaultHandler)
  server.getHandler.asInstanceOf[ContextHandlerCollection].addHandler(pluginHandler)
  server.stop
  server.start
  return pluginHandler
}


def runQuery(sql:String, sqlContext: SQLContext) = {
  sqlContext.sql(sql).collect.foreach(println)
}
