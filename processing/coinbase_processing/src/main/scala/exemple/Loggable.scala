package main.scala.exemple

trait Loggable {
  def log(message: String): Unit = println(s"[Log] $message")

  def logError(message: String): Unit = {
    val red = "\u001b[31m"
    val reset = "\u001b[0m"
    println(s"$red[Error] $message$reset")
  }
}
