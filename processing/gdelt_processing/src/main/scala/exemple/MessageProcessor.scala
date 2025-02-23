package main.scala.exemple

import org.apache.kafka.clients.consumer.ConsumerRecord

class MessageProcessor(gcsUploader: GCSUploader, sparkDataProcessor: SparkDataProcessor) extends Loggable {
  def processRecord(record: ConsumerRecord[String, String]): Unit = {
    try {
      if (record == null || record.value() == null || record.key() == null) {
        logError("Erreur : Record Kafka invalide (clé ou valeur null).")
        return
      }

      val topic = record.topic()
      val contentType = topic match {
        case "gdelt"    => "text/csv"
        case _          => "text/plain"
      }

      val keyType = record.key() match {
        case key if key.matches(".*export.*") => "export"
        case key if key.matches(".*mentions.*") => "mentions"
        case _ => "gkg"
      }

      log(s"Traitement des données GDELT avec la clé: ${record.key()}")

      try {
        sparkDataProcessor.processAndUploadToFirestore(record.value(), keyType)
      } catch {
        case e: Exception =>
          logError(s"Erreur lors du traitement Spark pour la clé ${record.key()}: ${e.getMessage}")
          throw e
      }

      try {
        gcsUploader.upload(record.value(), s"$topic/${record.key()}", contentType)
      } catch {
        case e: Exception =>
          logError(s"Erreur lors de l'upload GCS pour la clé ${record.key()}: ${e.getMessage}")
          throw e
      }
    } catch {
      case e: Exception =>
        logError(s"Erreur générale lors du traitement du record Kafka: ${e.getMessage}")
    }
  }
}
