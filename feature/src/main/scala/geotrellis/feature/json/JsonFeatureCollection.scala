package geotrellis.feature.json

import spray.json._

import geotrellis.feature._
import scala.util.{Try, Success, Failure}
import scala.collection.immutable.VectorBuilder
import FeatureFormats._
import DefaultJsonProtocol._

/**
  * Accumulates GeoJson from Feature class instances.
  *
  * Each individual feature is parametrized on a class we need to accumulate geoJson per
  * instance of an object in order to use implicit scope resolution in finding the correct format.
  *
 * This is the object that is created when GeoJson FeatureCollection is deserialized
 *
 * It aggregates feature objects with data member still encoded in json
 */
class JsonFeatureCollection(features: List[JsValue] = Nil) {

  var buffer = features

  //-- Used for Serialization
  def add[D: JsonWriter](feature: Feature[D]) =
    buffer = writeFeatureJson[D](feature) :: buffer
  def +=[D: JsonWriter](feature: Feature[D]) = add(feature)

  def addAll[D: JsonWriter](features: Seq[Feature[D]]) =
    features.foreach{ f => buffer = writeFeatureJson[D](f) :: buffer }
  def ++=[D: JsonWriter](features: Seq[Feature[D]]) = addAll(features)

  def toJson: JsValue =
    JsObject(
      "type" -> JsString("FeatureCollection"),
      "features" -> JsArray(buffer)
    )

  //-- Used for Deserialization
  def getAll[F <: Feature[_] :JsonFormat]: Vector[F] = {
    val ret = new VectorBuilder[F]()
    features.foreach{ f =>
      Try(f.convertTo[F]) match {
        case Success(feature) =>
          ret += feature
        case _ => //didn't match, live to fight another match
      }
    }
    ret.result()
  }

  def getAllPoints[D: JsonFormat]         = getAll[PointFeature[D]]
  def getAllLines[D: JsonFormat]          = getAll[LineFeature[D]]
  def getAllPolygons[D: JsonFormat]       = getAll[PolygonFeature[D]]
  def getAllMultiPoints[D: JsonFormat]    = getAll[MultiPointFeature[D]]
  def getAllMultiLines[D: JsonFormat]     = getAll[MultiLineFeature[D]]
  def getAllMultiPolygons[D: JsonFormat]  = getAll[MultiPolygonFeature[D]]
}

object JsonFeatureCollection{
  def apply() = new JsonFeatureCollection()

  def apply[D: JsonWriter](features: Traversable[Feature[D]]){
    val fc = new JsonFeatureCollection()
    fc ++= features.toList
  }
}