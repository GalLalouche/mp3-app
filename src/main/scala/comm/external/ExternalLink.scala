package comm.external

import spray.json.{DefaultJsonProtocol, JsonReader}

case class ExternalLink(host: String, main: String, extensions: Map[String, String])
object ExternalLink extends DefaultJsonProtocol {
  implicit val jsonReaderEv: JsonReader[ExternalLink] = jsonFormat3(ExternalLink.apply)
}
