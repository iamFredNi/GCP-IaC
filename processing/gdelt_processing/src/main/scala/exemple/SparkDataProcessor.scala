package main.scala.exemple

import sys.process._
import java.io._
import scala.io.Source
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{DocumentReference, FirestoreOptions}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, LongType, StringType, StructField, StructType}

import java.io.FileInputStream
import java.util

class SparkDataProcessor(spark: SparkSession) extends Loggable {
  private var cachedEvents: Option[Broadcast[DataFrame]] = None

  def processAndUploadToFirestore(record: String, keyType: String): Unit = {
    try {
      import spark.implicits._

      if (record == null || record.trim.isEmpty) {
        throw new IllegalArgumentException("Le record fourni est vide ou null.")
      }

      if (!Set("export", "mentions").contains(keyType)) {
        throw new IllegalArgumentException(s"Type de clé invalide : $keyType. Les valeurs acceptées sont 'export' ou 'mentions'.")
      }

      val dataset = spark.createDataset(record.split("\n").toSeq)
      val mentionsSchema = StructType(Array(
        StructField("event_id", LongType, nullable = false),
        StructField("event_datetime", StringType, nullable = false),
        StructField("mention_datetime", StringType, nullable = false),
        StructField("mention_type", IntegerType, nullable = true),
        StructField("mention_source_name", StringType, nullable = true),
        StructField("mention_identifier", StringType, nullable = true),
        StructField("sentence_id", IntegerType, nullable = true),
        StructField("actor1_char_offset", IntegerType, nullable = true),
        StructField("actor2_char_offset", IntegerType, nullable = true),
        StructField("action_char_offset", IntegerType, nullable = true),
        StructField("in_raw_text", IntegerType, nullable = true),
        StructField("confidence", DoubleType, nullable = true),
        StructField("mention_doc_len", IntegerType, nullable = true),
        StructField("mention_doc_tone", DoubleType, nullable = true)
      ))

      val eventsSchema = StructType(Array(
        StructField("event_id", LongType, nullable = false),
        StructField("event_date", IntegerType, nullable = false),
        StructField("month_year", IntegerType, nullable = true),
        StructField("year", IntegerType, nullable = true),
        StructField("fraction_date", DoubleType, nullable = true),
        StructField("actor1_code", StringType, nullable = true),
        StructField("actor1_name", StringType, nullable = true),
        StructField("actor1_country_code", StringType, nullable = true),
        StructField("actor1_known_group_code", StringType, nullable = true),
        StructField("actor1_ethnic_code", StringType, nullable = true),
        StructField("actor1_religion1_code", StringType, nullable = true),
        StructField("actor1_religion2_code", StringType, nullable = true),
        StructField("actor1_type1_code", StringType, nullable = true),
        StructField("actor2_code", StringType, nullable = true),
        StructField("actor2_name", StringType, nullable = true),
        StructField("actor2_country_code", StringType, nullable = true),
        StructField("actor2_known_group_code", StringType, nullable = true),
        StructField("actor2_ethnic_code", StringType, nullable = true),
        StructField("actor2_religion1_code", StringType, nullable = true),
        StructField("actor2_type1_code", StringType, nullable = true),
        StructField("is_root_event", IntegerType, nullable = true),
        StructField("event_code", StringType, nullable = true),
        StructField("quad_class", IntegerType, nullable = true),
        StructField("goldstein_scale", DoubleType, nullable = true),
        StructField("num_mentions", IntegerType, nullable = true),
        StructField("num_sources", IntegerType, nullable = true),
        StructField("avg_tone", DoubleType, nullable = true),
        StructField("actor1_geo_type", IntegerType, nullable = true),
        StructField("actor1_geo_fullname", StringType, nullable = true),
        StructField("actor1_geo_lat", DoubleType, nullable = true),
        StructField("actor1_geo_long", DoubleType, nullable = true),
        StructField("date_added", StringType, nullable = true),
        StructField("source_url", StringType, nullable = true)
      ))

      val schema = keyType match {
        case "export"    => eventsSchema
        case "mentions"  => mentionsSchema
      }

      validateDataset(dataset)
      val df: DataFrame = spark.read
        .option("header", "false")
        .option("delimiter", "\t")
        .schema(schema)
        .csv(dataset)
      validateDataFrame(df, schema)

      val transformedDF = keyType match {
        case "export" =>
          val transformedEvents = df
            .withColumn("event_date", to_date(col("event_date").cast("string"), "yyyyMMdd"))
            .withColumn("month_year", date_format(to_date(concat(
              substring(col("month_year"), 1, 4), lit("-"),
              substring(col("month_year"), 5, 2)
            ), "yyyy-MM"), "yyyy-MM"))

          log(s"\t - Données traitées")
          if (cachedEvents.isDefined) {
            val eventsBroadcast = cachedEvents.get.value
            val enrichedEvents = transformedEvents
              .join(eventsBroadcast, Seq("event_id"), "inner")
              .select(
                transformedEvents("*"),
                eventsBroadcast("mentions")
              )
            uploadToFirestore(enrichedEvents)
            log(s"\t - Fichier envoyé dans Firestore")
          } else {
            cachedEvents = Some(spark.sparkContext.broadcast(transformedEvents))
            transformedEvents
          }

        case "mentions" =>
          val transformedMentions = df
            .withColumn("event_date", date_format(to_date(substring(col("event_datetime"), 1, 8), "yyyyMMdd"), "yyyy-MM-dd"))
            .withColumn("mention_datetime", to_timestamp(col("mention_datetime").cast("string"), "yyyyMMddHHmmss"))
            .groupBy("event_id")
            .agg(
              collect_list(
                struct(
                  col("event_date"),
                  col("mention_datetime"),
                  col("mention_type"),
                  col("mention_source_name"),
                  col("mention_identifier"),
                  col("sentence_id"),
                  col("actor1_char_offset"),
                  col("actor2_char_offset"),
                  col("action_char_offset"),
                  col("in_raw_text"),
                  col("confidence"),
                  col("mention_doc_len"),
                  col("mention_doc_tone")
                )
              ).alias("mentions")
            )

          log(s"\t - Données traitées")
          if (cachedEvents.isDefined) {
            val eventsBroadcast = cachedEvents.get.value
            val enrichedMentions = transformedMentions
              .join(eventsBroadcast, Seq("event_id"), "inner")
              .select(
                eventsBroadcast("*"),
                transformedMentions("mentions")
              )
            uploadToFirestore(enrichedMentions)
            log(s"\t - Fichier envoyé dans Firestore")
          } else {
            cachedEvents = Some(spark.sparkContext.broadcast(transformedMentions))
            transformedMentions
          }
      }
    } catch {
      case e: Exception =>
        logError(s"Erreur lors du traitement ou de l'upload des données : ${e.getMessage}")
    }
  }

  private def validateDataset(dataset: Dataset[String]): Unit = {
    if (dataset.isEmpty) {
      throw new IllegalArgumentException("Le Dataset est vide.")
    }
    if (!dataset.first().contains("\t")) {
      throw new IllegalArgumentException("Les données ne contiennent pas le délimiteur tabulaire attendu.")
    }
  }

  private def validateDataFrame(df: DataFrame, schema: StructType): Unit = {
    val expectedColumns = schema.fields.map(_.name).toSet
    val actualColumns = df.columns.toSet
    val missingColumns = expectedColumns.diff(actualColumns)
    if (missingColumns.nonEmpty) {
      throw new IllegalArgumentException(s"Colonnes manquantes dans le DataFrame : ${missingColumns.mkString(", ")}")
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
          val keys_data = Array(
            "month_year", "year", "fraction_date", "actor1_code", "actor1_name", "actor1_country_code", "actor1_known_group_code",
            "actor1_ethnic_code", "actor1_religion1_code", "actor1_religion2_code", "actor1_type1_code", "actor2_code",
            "actor2_name", "actor2_country_code", "actor2_known_group_code", "actor2_ethnic_code", "actor2_religion1_code",
            "actor2_type1_code", "is_root_event", "event_code", "quad_class", "goldstein_scale", "num_mentions", "num_sources",
            "avg_tone", "actor1_geo_type", "actor1_geo_fullname", "actor1_geo_lat", "actor1_geo_long", "date_added", "source_url"
          )

          val transactionData = new util.HashMap[String, Any]()
          for (i <- keys_data.indices) {
            transactionData.put(keys_data(i), row(i + 2))
          }

          val mentions = row.getAs[Seq[Row]]("mentions").toList
          val mentionList = new util.ArrayList[util.HashMap[String, Any]]()

          mentions.foreach { mention =>
            val keys_mention = Array(
              "event_date", "mention_datetime", "mention_type", "mention_source_name", "mention_identifier", "sentence_id",
              "actor1_char_offset", "actor2_char_offset", "action_char_offset", "in_raw_text", "confidence",
              "mention_doc_len", "mention_doc_tone"
            )

            val mentionData = new util.HashMap[String, Any]()
            for (i <- keys_mention.indices) {
              mentionData.put(keys_mention(i), mention.getAs[Any](keys_mention(i)))
            }
            mentionList.add(mentionData)
          }
          transactionData.put("mentions", mentionList)

          val docData = new util.HashMap[String, Any]()
          docData.put("transactionData", transactionData)

          firestore
            .collection("dailyData")
            .document(row(1).toString)
            .collection("events")
            .document(row(0).toString)
            .set(docData)

          firestore
            .collection("dateIndex")
            .document(row(1).toString)
            .set(Map.empty[String, Any])
        }
      } finally {
        firestore.close()
      }
    }
  }
}
