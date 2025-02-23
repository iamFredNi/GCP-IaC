package test.scala.exemple

import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  test("Run GCSUploaderTest") {
    val gcsUploaderTest = new GCSUploaderTest
    gcsUploaderTest.execute()  // Appelez la méthode de test pour GCSUploader
  }

  test("Run MessageProcessorTest") {
    val messageProcessorTest = new MessageProcessorTest
    messageProcessorTest.execute()
  }

  test("Run SparkDataProcessorTest") {
    val sparkDataProcessorTest = new SparkDataProcessorTest
    sparkDataProcessorTest.execute()
  }

  test("Run KafkaConsumerServiceTest") {
    val kafkaConsumerServiceTest = new KafkaConsumerServiceTest
    kafkaConsumerServiceTest.execute()
  }

  test("Run LoggableTest") {
    val loggableTest = new LoggableTest
    loggableTest.execute()
  }
}
