package observatory

object Main extends App {
  val year = "/1975.csv"
  val stations = "/stations.csv"
  val res = Extraction.locateTemperatures(1975, stations, year)
  Extraction.locationYearlyAverageRecords(res)



}
