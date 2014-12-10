import java.io.{Writer, PrintStream}
import java.lang.CharSequence
object CaptureAllBuffer {
  val buffer = new StringBuffer()

  def append(x:String) = buffer.append(x)

  override def toString():String = buffer.toString

  def clear() = buffer.delete(0, buffer.length())
}

class DelegatingPrintStream(delegate:PrintStream) extends PrintStream(delegate) {
  override def println(x:java.lang.Object) {
    CaptureAllBuffer.append(String.valueOf(x))
    delegate.println(x)
  }
}

class DelegatingWriter(delegate:Writer) extends java.io.Writer {
  val buffer = new StringBuffer()

  override def append(c:Char):Writer = {
    buffer.append(c)
    delegate.append(c)
    return this
  }

  override def append(c:CharSequence):Writer = {
    buffer.append(c)
    delegate.append(c)
    return this
  }

  override def append(csq: CharSequence, start:Int, end:Int):Writer = {
    buffer.append(csq, start, end)
    delegate.append(csq, start, end)
    return this
  }

  override def close() = delegate.close
  override def flush() = delegate.flush

  override def write(cbuf: Array[Char]) = {
    buffer.append(cbuf)
    delegate.write(cbuf)
  }

  override def write(cbuf: Array[Char], offset:Int, length:Int) = {
    buffer.append(cbuf, offset, length)
    delegate.write(cbuf, offset, length)
  }


  override def write(c:Int) = {
    buffer.append(c)
    delegate.write(c)
  }

  override def write(str:String) = {
    buffer.append(str)
    delegate.write(str)
  }

  override def write(str:String, offset:Int, length:Int) = {
    buffer.append(str)
    delegate.write(str, offset, length)
  }

  def getValues():String = {
    return buffer.toString
  }

  def clear() = buffer.delete(0, buffer.length())

  def sync() = CaptureAllBuffer.append(buffer.toString)
}
