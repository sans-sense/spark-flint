import org.objectweb.asm._
import org.objectweb.asm.util._
import java.io._

object SourceUtil {
  def classLoader = SourceUtil.getClass.getClassLoader.asInstanceOf[scala.tools.nsc.interpreter.IMain$TranslatingClassLoader]

  def getClassBytes(className:String):Array[Byte] = {
    val bytes:Array[Byte] = classLoader.classBytes(className)
    return bytes
  }

  def print(className:String) = {
    val cReader = new ClassReader(new ByteArrayInputStream(getClassBytes(className)))
    cReader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0)
  }

  def source(className:String) = {

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
}
