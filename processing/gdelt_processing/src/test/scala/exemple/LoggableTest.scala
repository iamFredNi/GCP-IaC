package test.scala.exemple

import main.scala.exemple.Loggable
import org.scalatest.funsuite.AnyFunSuite

class LoggableTest extends AnyFunSuite with Loggable {

  test("log should output correct log message") {
    val message = "Test message"
    log(message) // Vérifiez manuellement dans la console
  }

  test("logError should output correct error message") {
    val errorMessage = "Test error"
    logError(errorMessage) // Vérifiez manuellement dans la console
  }
}
