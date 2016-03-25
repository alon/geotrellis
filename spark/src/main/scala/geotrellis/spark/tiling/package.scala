package geotrellis.spark

import geotrellis.vector._
import geotrellis.proj4._

/**
  * This package is concerned with translation of coordinates or extents between
  * geographic extents and the grid space represented by SpatialKey(col, row) coordinates,
  * the layout that defines that grid space, as well as functionality for cutting tiles into
  * a uniform grid space.
  */
package object tiling {
  private final val WORLD_WSG84 = Extent(-180, -89.99999, 179.99999, 89.99999)

  implicit class CRSWorldExtent(crs: CRS) {
    def worldExtent: Extent =
      crs match {
        case x if x == LatLng =>
          WORLD_WSG84
        case x if x == WebMercator =>
          Extent(-20037508.342789244, -20037508.342789244, 20037508.342789244, 20037508.342789244)
        case _ =>
          WORLD_WSG84.reproject(LatLng, crs)
      }
  }
}
