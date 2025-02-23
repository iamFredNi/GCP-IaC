package main.scala.exemple

import org.apache.kafka.clients.consumer.ConsumerRecord
import scala.collection.mutable.ListBuffer
import java.util.UUID

class MessageProcessor(gcsUploader: GCSUploader,sparkDataProcessor: SparkDataProcessor) extends Loggable {
  private var jsonRecords = ListBuffer[String]()
  private var previousKey: String = ""
  private val consumerId = UUID.randomUUID().toString

  def processRecord(record: ConsumerRecord[String, String]): Unit = {
    val topic = record.topic()
    val contentType = topic match {
      case "coinbase" => "application/json"
      case _          => "text/plain"
    }

    try {
      if (record.key() == null || record.value() == null) {
        logError("Erreur : Clé ou valeur du message est null")
        return
      }

      if (record.key() != previousKey && jsonRecords.nonEmpty) {
        val combinedJsonData = jsonRecords.mkString("[", ",", "]")
        val keyWithConsumerId = previousKey.split("\\.")(0) + "-" + consumerId + ".json"
        log(s"Traitement des données coinbase avec la key: ${keyWithConsumerId}")

        try {
          sparkDataProcessor.processAndUploadToFirestore(combinedJsonData)
        } catch {
          case e: Exception =>
            logError(s"Erreur lors du traitement des données avec Spark: ${e.getMessage}")
        }

        try {
          gcsUploader.upload(combinedJsonData, s"coinbase/$keyWithConsumerId", contentType)
        } catch {
          case e: Exception =>
            logError(s"Erreur lors de l'upload des données vers GCS avec la clé : $keyWithConsumerId, ${e.getMessage}")
        }
        jsonRecords.clear()
      }
      jsonRecords += record.value()
      previousKey = record.key()

    } catch {
      case e: Exception =>
        logError(s"Erreur générale lors du traitement du message avec la clé ${record.key()}: ${e.getMessage}")
    }
  }
}
