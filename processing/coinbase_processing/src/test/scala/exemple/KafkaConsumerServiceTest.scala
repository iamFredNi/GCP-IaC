package test.scala.exemple

import main.scala.exemple.{KafkaConsumerService, Loggable}
import org.scalatest.funsuite.AnyFunSuite
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.scalatest.matchers.must.Matchers.{be, noException}

class KafkaConsumerServiceTest extends AnyFunSuite with Loggable {

  test("pollMessages should return records without exception") {
    val kafkaService = new KafkaConsumerService("localhost:9092", "test-group", List("test-topic"))
    val records = kafkaService.pollMessages()
    assert(records.isEmpty || records.isInstanceOf[Iterable[ConsumerRecord[String, String]]])
  }

  test("close should execute without exception") {
    val kafkaService = new KafkaConsumerService("localhost:9092", "test-group", List("test-topic"))
    noException should be thrownBy kafkaService.close()
  }
}
