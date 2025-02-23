package main.scala.exemple

import org.apache.spark.sql.SparkSession

import java.io.{File, InputStream}
import java.nio.file.Files

object Main extends Loggable{

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      log("Erreur : L'argument du broker Kafka est requis. Exemple : 'localhost:9092'")
      System.exit(1)
    }
    val kafkaBroker = args(0)

    log("Starting GDELT processing")

    val resourceStream: InputStream = getClass.getClassLoader.getResourceAsStream("mmsdtd-5a84c3067a4a.json")
    require(resourceStream != null, "Le fichier JSON est introuvable dans resources.")

    // Écrire la ressource dans un fichier temporaire
    val tempFile = File.createTempFile("gcp-keyfile", ".json")
    Files.copy(resourceStream, tempFile.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    tempFile.deleteOnExit()

    val spark = SparkSession.builder()
      .master("local")
      .appName("topic_gdelt")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", tempFile.getAbsolutePath)
      .config("spark.executor.extraJavaOptions", "--add-opens java.base/java.nio=ALL-UNNAMED")
      .getOrCreate()

    val sparkDataProcessor = new SparkDataProcessor(spark)

    val kafkaService = new KafkaConsumerService(
      brokers = kafkaBroker,
      groupId = "gdelt-consumer-group",
      topics = List("gdelt")
    )

    val gcsUploader = new GCSUploader(
      bucketName = "test-kafka-sdtd",
      projectId = "mmsdtd",
      credentialsPath = tempFile.getAbsolutePath
    )

    val messageProcessor = new MessageProcessor(gcsUploader,sparkDataProcessor)

    try {
      var keysProcessed = Set[String]()
      var isProcessing = true

      while (isProcessing) {
        val records = kafkaService.pollMessages()
        for (record <- records) {
          keysProcessed += record.key()
          messageProcessor.processRecord(record)
        }

        if (keysProcessed.size >= 2) {
          isProcessing = false
        }
      }
    } finally {
      kafkaService.close()
      log("Shutting down application")
    }
  }
}
