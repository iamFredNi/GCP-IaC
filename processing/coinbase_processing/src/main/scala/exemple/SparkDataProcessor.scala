package main.scala.exemple

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import org.apache.spark.sql.functions.{avg, collect_list}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonProcessingException

import java.io.FileInputStream
import java.time.LocalDate
import java.util
import scala.collection.mutable

class SparkDataProcessor(spark: SparkSession) extends Loggable {
  import spark.implicits._
  def processAndUploadToFirestore(jsonData: String): Unit = {
    try {
      if (jsonData.isEmpty || !isValidJson(jsonData)) {
        logError(s"Erreur : Données JSON invalides ou vides. JSON: $jsonData")
        return
      }

      val jsonDataset = spark.createDataset(Seq(jsonData))
      val df: DataFrame = spark.read.json(jsonDataset)
      val cleanedDf = df.na.drop()

      if (!validateColumns(cleanedDf)) {
        logError("Arrêt du traitement en raison de colonnes invalides dans les données.")
        return
      }

      val groupedDf = cleanedDf
        .groupBy("product_id", "time")
        .agg(
          avg("price").alias("price"),
          avg("best_ask").alias("best_ask"),
          avg("best_ask_size").alias("best_ask_size"),
          avg("best_bid").alias("best_bid"),
          avg("best_bid_size").alias("best_bid_size"),
          avg("high_24h").alias("high_24h"),
          avg("last_size").alias("last_size"),
          avg("low_24h").alias("low_24h"),
          avg("open_24h").alias("open_24h"),
          avg("volume_24h").alias("volume_24h"),
          avg("volume_30d").alias("volume_30d"),
          collect_list("side").alias("side"),
          collect_list("trade_id").alias("trade_id"),
          collect_list("type").alias("type")
        )
      log(s"\t - Données traitées")
      uploadToFirestore(groupedDf)
      log(s"\t - Fichier envoyé dans Firestore")
    }catch{
      case e: Exception =>
        logError(s"Erreur lors du traitement des données ou de l'upload dans Firestore: ${e.getMessage}")
        throw e
    }
  }

  private def validateColumns(df: DataFrame): Boolean = {
    val expectedColumns = Seq(
      "product_id", "time", "price", "best_ask", "best_ask_size", "best_bid",
      "best_bid_size", "high_24h", "last_size", "low_24h", "open_24h",
      "volume_24h", "volume_30d", "side", "trade_id", "type", "sequence"
    )

    val actualColumns = df.columns
    val missingColumns = expectedColumns.filterNot(actualColumns.contains)
    if (missingColumns.nonEmpty) {
      logError(s"Erreur : Colonnes manquantes dans le DataFrame. Colonnes attendues : ${expectedColumns.mkString(", ")}, Colonnes trouvées : ${actualColumns.mkString(", ")}, Colonnes manquantes : ${missingColumns.mkString(", ")}")
      return false
    }

    val unexpectedColumns = actualColumns.filterNot(expectedColumns.contains)
    if (unexpectedColumns.nonEmpty) {
      logError(s"Erreur : Colonnes inattendues trouvées dans le DataFrame. Colonnes attendues : ${expectedColumns.mkString(", ")}, Colonnes trouvées : ${actualColumns.mkString(", ")}, Colonnes inattendues : ${unexpectedColumns.mkString(", ")}")
      return false
    }
    //log(s"Toutes les colonnes attendues sont présentes. Colonnes trouvées : ${actualColumns.mkString(", ")}")
    true
  }

  private def isValidJson(jsonData: String): Boolean = {
    val objectMapper = new ObjectMapper()
    try {
      objectMapper.readTree(jsonData)
      true
    } catch {
      case e: JsonProcessingException =>
        logError(s"Le JSON fourni est invalide : ${e.getMessage}")
        false
      case e: Exception =>
        logError(s"Erreur inattendue lors de la validation du JSON : ${e.getMessage}")
        false
    }
  }

  private def uploadToFirestore(data : DataFrame): Unit = {
    val resourceStream = getClass.getClassLoader.getResourceAsStream("mmsdtd-5a84c3067a4a.json")
    val credentials = GoogleCredentials.fromStream(resourceStream)
    data.foreachPartition { (partition: Iterator[Row]) =>
      val firestore = FirestoreOptions.newBuilder()
        .setCredentials(credentials)
        .build()
        .getService

      try {
        partition.foreach { row =>
          val timestamp = row(1).toString

          val keys = Array("price", "best_ask", "best_ask_size", "best_bid", "best_bid_size", "high_24h", "last_size", "low_24h", "open_24h", "volume_24h", "volume_30d")
          val transactionData = new util.HashMap[String, Any]()

          for (i <- keys.indices) {
            transactionData.put(keys(i), row(i + 2))
          }

          val sideList = new util.ArrayList[String]()
          row(13).asInstanceOf[scala.collection.Seq[String]].foreach(sideList.add)

          val tradeIdList = new util.ArrayList[Long]()
          row(14).asInstanceOf[scala.collection.Seq[Long]].foreach(tradeIdList.add)

          val typeList = new util.ArrayList[String]()
          row(15).asInstanceOf[scala.collection.Seq[String]].foreach(typeList.add)

          transactionData.put("side", sideList)
          transactionData.put("trade_id", tradeIdList)
          transactionData.put("type", typeList)

          val docData = new util.HashMap[String, Any]()
          docData.put("transactionData", transactionData)

          firestore
            .collection("dailyData")
            .document(LocalDate.now.toString)
            .collection("currencies")
            .document(row(0).toString)
            .collection("timestamps")
            .document(timestamp)
            .set(docData)

          firestore
            .collection("dateIndex")
            .document(LocalDate.now.toString)
            .set(Map.empty[String, Any])

          firestore
            .collection("currencieIndex")
            .document(row(0).toString)
            .set(Map.empty[String, Any])
        }
      } finally {
        firestore.close()
      }
    }
  }
}

