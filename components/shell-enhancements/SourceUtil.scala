import org.objectweb.asm._
import org.objectweb.asm.util._
import java.io._

object SourceUtil {
  def classLoader = SourceUtil.getClass.getClassLoader.asInstanceOf[org.apache.spark.repl.SparkIMain$TranslatingClassLoader]
  val indexes: collection.mutable.Map[String, String]  = collection.mutable.Map()

  def getClassBytes(className:String):Array[Byte] = {
    val bytes:Array[Byte] = classLoader.classBytes(className)
    return bytes
  }

  def print(className:String, printer : PrintWriter = new PrintWriter(System.out)) = {
    val cReader = new ClassReader(new ByteArrayInputStream(getClassBytes(className)))
    cReader.accept(new TraceClassVisitor(printer), 0)
  }

  def printType(obj:Any) = {
    print(obj.getClass.getName)
  }

  def source(obj:Any) = {
    val stringWriter = new StringWriter()
    val sourceRegex = """.*compiled from:\s+(\w+)\.(java|scala)""".r
    print(obj.getClass.getName, new PrintWriter(stringWriter))
    stringWriter.toString.split("\n").foreach {
      line => sourceRegex findFirstIn line match {
        case Some(s) => val sourceRegex(fileName, extension) = s; printSourceForPath(indexes(fileName + "." + extension))
        case None => //do nothing
      }
    }
  }

  def printSelf() = {
    print(this.getClass.getName)
  }

  def dumpClassBytes(className:String, path:String) = {
    val bytes:Array[Byte] = classLoader.classBytes(className)
    val stream = new FileOutputStream(path);
    try {
      stream.write(bytes);
    } finally {
      stream.close();
    }
  }

  def indexFolder(path:String) = {
    indexes ++= accumulateFiles(new File(path))
  }

  private def printSourceForPath(filePath:String) = {
    val source = scala.io.Source.fromFile(filePath)
    source.getLines.foreach{
         println
    }
    source.close
  }

  private def accumulateFiles(f: File): Map[String,String] = {
    val these = f.listFiles
    these.filter(_.isDirectory == false ).foldLeft(Map():Map[String,String])((r,file) => if (file.getName.endsWith("scala") || file.getName.endsWith("java")) { r ++ Map(file.getName-> file.getAbsolutePath)} else r) ++ these.filter(_.isDirectory).foldLeft(Map():Map[String,String])((r,file) => r ++ accumulateFiles(file))
  }
    
}
