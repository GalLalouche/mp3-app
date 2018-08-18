package player

import java.io.File

import spray.json._

sealed trait Song {
  def title: String
  def artistName: String
  def albumName: String
  def track: Int
  def year: Int
  def bitrate: String
  def duration: Int // in seconds
  def size: Long // In bytes
  def trackGain: Option[Double]

  def totalLengthInMicroSeconds: Long = duration * 1e6.toLong
  // TODO solve this less hackishly
  def formattedLength: String = TimeChange(totalLengthInMicroSeconds, Long.MaxValue).toDoubleDigitDisplay
  def remotePath: String
}

case class RemoteSong(
    override val title: String,
    override val artistName: String,
    override val albumName: String,
    override val track: Int,
    override val year: Int,
    override val bitrate: String,
    override val duration: Int, // in seconds
    override val size: Long, // In bytes
    override val trackGain: Option[Double],
    poster: String,
    mp3: Option[String],
    flac: Option[String],
) extends Song {
  override def remotePath: String = mp3.orElse(flac).get
}

object RemoteSong extends DefaultJsonProtocol {
  implicit val jsonReaderEv: JsonReader[RemoteSong] = jsonFormat12(RemoteSong.apply)
}

case class LocalSong(
    override val title: String,
    override val artistName: String,
    override val albumName: String,
    override val track: Int,
    override val year: Int,
    override val bitrate: String,
    override val duration: Int, // in seconds
    override val size: Long, // In bytes
    override val trackGain: Option[Double],
    file: File,
    localPosterPath: File,
    override val remotePath: String,
) extends Song
