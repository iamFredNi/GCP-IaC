package main.scala.exemple

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}

import java.util.Properties
import scala.collection.JavaConverters._

class KafkaConsumerService(brokers: String, groupId: String, topics: List[String]) extends Loggable {
  private val kafkaProps = new Properties()
  kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
  kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
  kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

  private val consumer = new KafkaConsumer[String, String](kafkaProps)
  consumer.subscribe(topics.asJava)

  def pollMessages(): Iterable[ConsumerRecord[String, String]] = {
    try {
      val records = consumer.poll(java.time.Duration.ofMillis(1000))
      records.asScala
    } catch {
      case e: Exception =>
        logError(s"Erreur lors du polling des messages Kafka: ${e.getMessage}")
        Iterable.empty
    }
  }

  def close(): Unit = {
    log("Closing Kafka consumer")
    consumer.close()
  }
}
