package main.scala.exemple

import org.apache.spark.sql.SparkSession
import java.io.{File, InputStream}
import java.nio.file.{Files, Paths}

object Main extends Loggable{

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      logError("Erreur : L'argument du broker Kafka est requis. Exemple : 'localhost:9092'")
      System.exit(1)
    }
    val kafkaBroker = args(0)

    log("Starting Coinbase processing")

    val resourceStream: InputStream = getClass.getClassLoader.getResourceAsStream("mmsdtd-5a84c3067a4a.json")
    require(resourceStream != null, "Le fichier JSON est introuvable dans resources.")

    // Écrire la ressource dans un fichier temporaire
    val tempFile = File.createTempFile("gcp-keyfile", ".json")
    Files.copy(resourceStream, tempFile.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    tempFile.deleteOnExit()

    val spark = SparkSession.builder()
      .master("local")
      .appName("topic_coinbase")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", tempFile.getAbsolutePath)
      .config("spark.driver.extraJavaOptions", "--add-opens java.base/javax.security.auth=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED")
      .getOrCreate()

    val sparkDataProcessor = new SparkDataProcessor(spark)

    val kafkaService = new KafkaConsumerService(
      brokers = kafkaBroker,
      groupId = "coinbase-consumer-group",
      topics = List("coinbase")
    )

    val gcsUploader = new GCSUploader(
      bucketName = "test-kafka-sdtd",
      projectId = "mmsdtd",
      credentialsPath = tempFile.getAbsolutePath
    )

    val messageProcessor = new MessageProcessor(gcsUploader,sparkDataProcessor)

    try {
      while (true) {
        val records = kafkaService.pollMessages()
        for (record <- records) {
          messageProcessor.processRecord(record)
        }
      }
    } finally {
      kafkaService.close()
      log("Shutting down application")
    }
  }
}
