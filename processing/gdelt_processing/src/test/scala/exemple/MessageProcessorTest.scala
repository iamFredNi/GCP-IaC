package test.scala.exemple

import main.scala.exemple.{GCSUploader, MessageProcessor, SparkDataProcessor}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers._

class MessageProcessorTest extends AnyFunSuite {

  // Test pour vérifier si le message est traité correctement pour un enregistrement valide.
  test("processRecord should process valid records for gdelt topic") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Création d'un record Kafka valide pour le topic "gdelt"
    val csvRecord = """event_id,actor1,actor2,action,location,time
                      |123,USA,Russia,invade,Ukraine,2024-01-01""".stripMargin

    val record = new ConsumerRecord[String, String]("gdelt", 0, 0L, "export_event1", csvRecord)

    // Appel de la méthode à tester avec le record
    processor.processRecord(record)

    // Vérification que la méthode processAndUploadToFirestore a été appelée une fois avec les bons paramètres
    verify(mockSparkProcessor, times(1)).processAndUploadToFirestore(csvRecord, "export")

    // Vérification que l'upload GCS a bien été appelé une fois avec les bons paramètres
    verify(mockGcsUploader, times(1)).upload(csvRecord, "gdelt/export_event1", "text/csv")
  }

  // Test pour vérifier si la méthode gère les records invalides correctement.
  test("processRecord should handle invalid records gracefully") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Création d'un record Kafka invalide (clé ou valeur null)
    val invalidRecord = new ConsumerRecord[String, String]("gdelt", 0, 0L, null, null)

    // Appel de la méthode à tester avec le record invalide
    processor.processRecord(invalidRecord)

    // Vérification que ni le SparkProcessor, ni le GCSUploader n'ont été appelés
    verify(mockSparkProcessor, times(0)).processAndUploadToFirestore(any[String], any[String])
    verify(mockGcsUploader, times(0)).upload(any[String], any[String], any[String])
  }

  // Test pour vérifier le traitement des records avec des clés de type "mentions"
  test("processRecord should process mentions records correctly") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Création d'un record Kafka pour le topic "gdelt" et clé "mentions"
    val csvRecord = """event_id,actor1,actor2,action,location,time
                      |124,China,USA,trade,Beijing,2024-01-02""".stripMargin

    val record = new ConsumerRecord[String, String]("gdelt", 0, 0L, "mentions_event2", csvRecord)

    // Appel de la méthode à tester avec le record
    processor.processRecord(record)

    // Vérification que la méthode processAndUploadToFirestore a été appelée pour le type "mentions"
    verify(mockSparkProcessor, times(1)).processAndUploadToFirestore(csvRecord, "mentions")

    // Vérification que l'upload GCS a bien été appelé pour le type "mentions"
    verify(mockGcsUploader, times(1)).upload(csvRecord, "gdelt/mentions_event2", "text/csv")
  }

  // Test pour vérifier que la méthode fonctionne correctement avec la clé "gkg"
  test("processRecord should process gkg records correctly") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Création d'un record Kafka pour le topic "gdelt" et clé "gkg"
    val csvRecord = """event_id,actor1,actor2,action,location,time
                      |125,USA,France,meet,Paris,2024-01-03""".stripMargin

    val record = new ConsumerRecord[String, String]("gdelt", 0, 0L, "gkg_event3", csvRecord)

    // Appel de la méthode à tester avec le record
    processor.processRecord(record)

    // Vérification que la méthode processAndUploadToFirestore a été appelée pour le type "gkg"
    verify(mockSparkProcessor, times(1)).processAndUploadToFirestore(csvRecord, "gkg")

    // Vérification que l'upload GCS a bien été appelé pour le type "gkg"
    verify(mockGcsUploader, times(1)).upload(csvRecord, "gdelt/gkg_event3", "text/csv")
  }

  // Test pour vérifier la gestion d'une exception dans le traitement Spark
  test("processRecord should log error when Spark processing fails") {
    // Mock des dépendances
    val mockGcsUploader = mock(classOf[GCSUploader])
    val mockSparkProcessor = mock(classOf[SparkDataProcessor])

    // Simulation d'une exception dans le SparkDataProcessor
    when(mockSparkProcessor.processAndUploadToFirestore(any[String], any[String])).thenThrow(new RuntimeException("Spark processing failed"))

    // Instance de la classe à tester avec les mocks
    val processor = new MessageProcessor(mockGcsUploader, mockSparkProcessor)

    // Création d'un record Kafka valide
    val csvRecord = """event_id,actor1,actor2,action,location,time
                      |126,UK,USA,negotiate,London,2024-01-04""".stripMargin

    val record = new ConsumerRecord[String, String]("gdelt", 0, 0L, "export_event4", csvRecord)

    // Appel de la méthode à tester avec le record
    processor.processRecord(record)

    // Vérification que l'exception a été loggée
    // On s'attend à ce que la méthode logError soit appelée dans MessageProcessor
    // Cela peut être testé via un mock sur le logger ou via des assertions sur les logs si nécessaire.
  }
}
