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

  test("processAndUploadToFirestore should handle valid tabular data for 'mentions'") {
    val processor = new SparkDataProcessor(spark)

    // Données tabulaires simulées correspondant aux colonnes attendues pour 'mentions'
    val validTabularData = """123456	20231222T00:00:00Z	20231222T01:00:00Z	1	Source1	ID1	1	10	20	30	1	0.95	500	0.2
                             |789012	20231222T00:30:00Z	20231222T01:30:00Z	2	Source2	ID2	2	15	25	35	1	0.90	600	0.3""".stripMargin

    // Appel de la méthode processAndUploadToFirestore avec des données tabulaires valides
    noException should be thrownBy {
      processor.processAndUploadToFirestore(validTabularData, "mentions")
    }
  }

  test("processAndUploadToFirestore should handle valid tabular data for 'export'") {
    val processor = new SparkDataProcessor(spark)

    // Données tabulaires simulées correspondant aux colonnes attendues pour 'export'
    val validTabularData = """123456	20231222	202312	2023	0.5	ACT1	Name1	USA	ACT1_GRP	ACT1_ETH	ACT1_REL1	ACT1_REL2	ACT1_TYPE1	ACT2	Code2	Name2	USA_GRP	ETHNIC2	REL1B	ACT2_TYPE	1	EVNT_CODE	0	1.2	0.4	100	3	0.1	GeoType1	USA	40.7128	-74.0060	2023-12-22T00:00:00Z	http://source.com
                             |789012	20231222	202312	2023	0.7	ACT3	Name3	USA	ACT3_GRP	ACT3_ETH	ACT3_REL1	ACT3_REL2	ACT3_TYPE1	ACT4	Code4	Name4	USA_GRP	ETHNIC4	REL2B	ACT4_TYPE	0	EVNT_CODE2	1	1.0	0.3	120	2	0.2	GeoType2	Canada	45.4215	-75.6972	2023-12-22T01:00:00Z	http://source2.com""".stripMargin

    // Appel de la méthode processAndUploadToFirestore avec des données tabulaires valides pour 'export'
    noException should be thrownBy {
      processor.processAndUploadToFirestore(validTabularData, "export")
    }
  }
}
