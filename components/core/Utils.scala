import java.io._
import javax.servlet.ServletException
import javax.servlet.http._
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler._
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

class TabHackHandler(basePath:String = "./static/") extends org.eclipse.jetty.server.handler.AbstractHandler {
  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override def handle(target :String, baseRequest: Request,  request : HttpServletRequest, response : HttpServletResponse)  = {
    val source = scala.io.Source.fromFile(basePath + "sorttable.js")
    val lines = source.getLines mkString "\n"
    response.setContentType("application/x-javascript")
    source.close()
    response.getWriter().println(lines)
    response.setStatus(HttpServletResponse.SC_OK);
    baseRequest.setHandled(true);
  }
}

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


def addContextHandler(server:Server, path:String):ContextHandler = {
  // for faster dev, adds a ctx handler, so that we can change the actual handler as we develop
  val ctxHandler = new ContextHandler(path)
  ctxHandler.setHandler(new DefaultHandler)
  server.getHandler.asInstanceOf[ContextHandlerCollection].addHandler(ctxHandler)
  return ctxHandler
}

def addTabDisplayHack(server:Server) = {
  val jsReplaceHackCtx = addHandler(server, "/static/sorttable.js")
  jsReplaceHackCtx.setHandler(new TabHackHandler)
}

def runQuery(sql:String, sqlContext: SQLContext) = {
  sqlContext.sql(sql).collect.foreach(println)
}


class Component(name:String, help:String) {
  override def toString:String = {
    return s"$name $help"
  }
}

object ComponentManager {
  private val registry:collection.mutable.Map[String, Component] = collection.mutable.Map()

  def register(name:String, help:String) = {
    registry(name) = new Component(name, help)
  }

  def listComponents():Iterable[String] = {
    return registry.keys
  }

  def describe(name:String):Component = {
    return registry(name)
  }
}


