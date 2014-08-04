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

  def print(className:String) = {
    val cReader = new ClassReader(new ByteArrayInputStream(getClassBytes(className)))
    cReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0)
  }

  def printType(obj:Any) = {
    print(obj.getClass.getName)
  }

  def source(className:String) = {
    printSourceForPath(indexes(className))
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
