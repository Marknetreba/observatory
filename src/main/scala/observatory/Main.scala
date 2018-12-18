package observatory

object Main extends App {
  val year = "/1975.csv"
  val stations = "/stations.csv"
  val res = Extraction.locateTemperatures(1975, stations, year)
  val yearly = Extraction.locationYearlyAverageRecords(res)


 val location = Interaction.tileLocation(Tile(0, 0, 0))


}
