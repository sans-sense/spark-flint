import java.io.{IOException}
import javax.servlet.ServletException
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.eclipse.jetty.server.{Request, Server}
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler, DefaultHandler, ContextHandlerCollection}
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkContext

class TabHackHandler(basePath:String = "./static/") extends AbstractHandler {
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

def changeHandler(handler: AbstractHandler, actualHandler:ContextHandler, server: Server) = {
  server.stop
  actualHandler.setHandler(handler)
  server.start
}

// all  interesting things are private, so let's break it
def allow(field: java.lang.reflect.Field)=field.setAccessible(true)

def getServer(sc: org.apache.spark.SparkContext):Server = {
  val uiField = sc.getClass.getDeclaredField("ui");
  val serverInfoField = Class.forName("org.apache.spark.ui.WebUI").getDeclaredField("serverInfo")
  val serverField = Class.forName("org.apache.spark.ui.ServerInfo").getDeclaredField("server")
  List(uiField, serverInfoField, serverField).foreach{ field => allow(field)}

  val ui = uiField.get(sc) match { case Some(ui) => ui}
  val serverInfo = serverInfoField.get(ui) match { case Some(serverInfo) => serverInfo }
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
  val jsReplaceHackCtx = addContextHandler(server, "/static/sorttable.js")
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


