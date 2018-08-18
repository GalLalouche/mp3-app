package comm.external

import java.time.MonthDay
import java.time.format.DateTimeFormatter

import comm.RichJson._
import spray.json.{JsValue, JsonReader}

// TODO move to common
case class ExternalLinks(artistLinks: SingleEntityExternalLinks, albumLinks: SingleEntityExternalLinks) {
  require(artistLinks.entityType == ExternalLinkEntity.Artist)
  require(albumLinks.entityType == ExternalLinkEntity.Album)
}
object ExternalLinks {
  implicit object JsonReaderEv extends JsonReader[ExternalLinks] {
    override def read(json: JsValue): ExternalLinks = {
      def readSingleEntity(entityType: ExternalLinkEntity, json: JsValue): SingleEntityExternalLinks = {
        val links = json.asJsObject.fields.filter(_._1 != "timestamp").map(_._2.convertTo[ExternalLink]).toVector
        val timestamp = MonthDay parse(json str "timestamp", DateTimeFormatter ofPattern "dd/MM")
        SingleEntityExternalLinks(entityType, links, timestamp)
      }
      ExternalLinks(
        artistLinks = readSingleEntity(ExternalLinkEntity.Artist, json / "Artist links"),
        albumLinks = readSingleEntity(ExternalLinkEntity.Album, json / "Album links")
      )
    }
  }
}
