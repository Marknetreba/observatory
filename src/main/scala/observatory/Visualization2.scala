package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import scala.math._

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    point.y*((1.0-point.x)*d01 + point.x*d11) + (1.0-point.y)*((1.0-point.x)*d00 + point.x*d10)
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val width = 256
    val height = 256

    val pixels = (0 until width*height).par.map(index => {
      val (x_pos,y_pos) = ((index % width).toDouble/width + tile.x, (index / height).toDouble/height + tile.y)
      val location = {
        val n = 1 << tile.zoom
        val latRad = math.atan(math.sinh(math.Pi*(1.0 - 2.0*y_pos/n)))
        Location(latRad*180.0/math.Pi, x_pos*360.0/n - 180.0)
      }
      val (x0, x1, y1, y0) = (math.floor(location.lon).toInt, math.ceil(location.lon).toInt,
        math.floor(location.lat).toInt, math.ceil(location.lat).toInt)

      val d00: Temperature = grid(GridLocation(y0,x0))
      val d01: Temperature = grid(GridLocation(y1,x0))
      val d10: Temperature = grid(GridLocation(y0,x1))
      val d11: Temperature = grid(GridLocation(y1,x1))

      val predTemp = bilinearInterpolation(CellPoint(location.lon - x0, y0 - location.lat), d00,d01,d10,d11)
      val predColor = Visualization.interpolateColor(colors, predTemp)

      Pixel.apply(predColor.red, predColor.green, predColor.blue, 128)
    }).toArray


    Image.apply(width, height, pixels)

  }

}
