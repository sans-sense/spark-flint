import java.io.{FileInputStream, InputStream}
import collection.mutable
import java.util.jar.JarFile

import scala.collection.JavaConversions._
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

// ~/.m2/repository/org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar

object FileClassReader {
  def main(args:Array[String]) {
    val classReader = new ClassReader(
	  new FileInputStream(
		"../ClassifierManager.class"))
    processClass(classReader)
  }

  def processClass(cr:ClassReader):Array[MethodNode] = {
    val visitor = new CapturingClassVisitor(Opcodes.ASM5)
    cr.accept(visitor, 0)
    visitor.getThem
  }

  def processJar(jarPath:String, shouldWriteToFile:Boolean = true): Array[MethodNode] = {
    val jarFile = new JarFile(jarPath)
    val methods = collection.mutable.Set[MethodNode]()

    jarFile.entries.filter(_.getName.endsWith(".class")) foreach { x =>
      methods ++= extractMethods(x.getName, jarFile.getInputStream(x)).toSet
    }
    val methodArray = methods.toArray
    if (shouldWriteToFile) {
      writeToFile(methodArray)
    }
    methodArray
  }

  def extractMethods(fName: String, is: InputStream):Array[MethodNode] = {
    val classReader = new ClassReader(is)
    processClass(classReader)
  }

  def writeToFile(methods:Array[MethodNode], fileName:String = "./code-analysis/methodChain.txt") {
    val pw = new java.io.PrintWriter(new java.io.FileWriter(fileName, false))
    try methods.foreach{mn => pw.write(s"**Method**\n $mn \n **Invokes**\n"); mn.getInvokedMethods.foreach{invokedMethod => pw.write(s" $invokedMethod\n")}; pw.write("\n")} finally pw.close()
  }

  class MethodNode(name:String) {
    private val invokedMethodNames:collection.mutable.Set[String] = collection.mutable.Set()

    override def toString():String = {
      return s"$name"
    }

    def addInvokedMethod(signature:String) {
      invokedMethodNames += signature
    }

    def getInvokedMethods():Array[String] = {
      return invokedMethodNames.toArray
    }
  }

  class CapturingClassVisitor(api:Int) extends ClassVisitor(api){

    val mv = new CapturingMethodVisitor(Opcodes.ASM5)
    var owner = ""

    override def visit( version:Int,access:Int,name:String, signature:String, superName:String,interfaces:Array[String]) {
      owner = name
      super.visit(version, access, name, signature, superName, interfaces)
    }

    override def visitMethod(access:Int, name:String, desc:String, signature:String, exceptions:Array[String]):MethodVisitor = {
      mv.visitStart(s"$owner $access $name $desc")
      return mv
    }

    def getThem():Array[MethodNode] = {
      return mv.getThem.toArray
    }
    
  }

  class CapturingMethodVisitor(api:Int) extends MethodVisitor(api) {
    val methods = collection.mutable.ListBuffer[MethodNode]()

    override def visitMethodInsn(opcode:Int, owner:String, name:String, desc:String, itf:Boolean) = {
      val methodNode = methods.last
      methodNode.addInvokedMethod(s"$owner $name $desc")
    }

    override def visitEnd() {
    }

    def visitStart(methodName:String) {
      methods += new MethodNode(methodName)
    }

    def getThem:Array[MethodNode] = {
      return methods.toArray
    }

    override def toString():String = {
      return s"$methods"
    }

  }

}
