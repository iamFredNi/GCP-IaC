package test.scala.exemple

import main.scala.exemple.{SparkDataProcessor, Loggable}
import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.{be, noException}

class SparkDataProcessorTest extends AnyFunSuite with Loggable {

  val spark: SparkSession = SparkSession.builder()
    .appName("SparkDataProcessorTest")
    .master("local")
    .getOrCreate()

  test("processAndUploadToFirestore should handle valid JSON with explicit schema") {
    val processor = new SparkDataProcessor(spark)

    // JSON valide correspondant aux colonnes attendues
    val validJsonData = """[
    {"product_id":"BTC-USD", "time":"2023-01-01T00:00:00Z", "price":10000, "best_ask":10100, "best_ask_size":0.1,
     "best_bid":9990, "best_bid_size":0.2, "high_24h":10500, "last_size":0.1, "low_24h":9800, "open_24h":10200,
     "volume_24h":1500, "volume_30d":10000, "side":"buy", "trade_id":12345, "type":"market", "sequence":67890}
    ]"""

    // Exécution du test sans exception
    noException should be thrownBy processor.processAndUploadToFirestore(validJsonData)
  }
}
