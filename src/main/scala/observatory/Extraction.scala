package observatory

import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.sql.SparkSession

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val spark: SparkSession = SparkSession
    .builder()
    .appName("Observatory")
    .master("local[2]")
    .config("spark.executor.memory", "1G")
    .config("spark.driver.memory", "2g")
    .getOrCreate()

  import spark.implicits._

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */

  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String):Iterable[(LocalDate, Location, Temperature)] = {


    val stations = spark.read.csv(Paths.get(getClass.getResource(stationsFile).getPath).toString)
      .toDF("STN","WBAN","latitude","longitude")

    val temp = spark.read.csv(Paths.get(getClass.getResource(temperaturesFile).getPath).toString)
      .toDF("STN","WBAN","month","day","temp")

    stations.join(temp, Seq("STN","WBAN"))
      .na.drop()
      .collect()
      .map(i => (LocalDate.of(year,i.get(4).toString.toInt,i.get(5).toString.toInt),
        Location(i.get(2).toString.toDouble, i.get(3).toString.toDouble),(i.get(6).toString.toDouble - 32)*5/9))
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    val res = records.toSeq.map(i => (i._2,i._3))
      .toDF("location","temp")
      .groupBy("location")
      .mean("temp")

    res.as[(Location, Double)].collect()
  }
}
