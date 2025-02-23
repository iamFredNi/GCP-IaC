package test.scala.exemple

import main.scala.exemple.{GCSUploader, SparkDataProcessor, MessageProcessor}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.funsuite.AnyFunSuite

class MessageProcessorTest extends AnyFunSuite {

  test("processRecord should handle valid records correctly") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Exemple de record Kafka valide
    val record1 = new ConsumerRecord[String, String]("coinbase", 0, 0L, "key1", """{"product_id":"BTC-USD","price":10000}""")
    val record2 = new ConsumerRecord[String, String]("coinbase", 0, 0L, "key2", """{"product_id":"BTC-USD","price":15000}""")

    // Appel de la méthode à tester avec deux enregistrements
    processor.processRecord(record1)
    processor.processRecord(record2)

    // Vérification que la méthode processAndUploadToFirestore a bien été appelée
    // Le mock devrait avoir été invoqué car le changement de key (de "key1" à "key2") remplit la condition
    verify(mockSparkProcessor, times(1)).processAndUploadToFirestore(any[String])
  }
}
