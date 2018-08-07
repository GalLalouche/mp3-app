package player

import spray.json._

case class Song(
    title: String,
    artistName: String,
    albumName: String,
    track: Int,
    year: Int,
    bitrate: String,
    duration: Int, // in seconds
    size: Long, // In bytes
    trackGain: Double,
    poster: String,
    mp3: Option[String],
    flac: Option[String],
) {
  // TODO solve this less hackishly
  def formattedLength: String =
    TimeChange.fromMicrosecond(duration * 1e6.toLong).toDoubleDigitDisplay

  def path: String = mp3.orElse(flac).get
}

object Song extends DefaultJsonProtocol {
  implicit val jsonReaderEv: JsonReader[Song] = jsonFormat12(Song.apply)
}
