package player.pkg

import java.io.File
import java.util.logging.{Level, Logger}

import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import player.LocalSong

import scala.collection.JavaConverters._

// TODO handle duplication with streamer
private object SongParser {
  Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF)

  /** Parses ID3 data */
  def apply(file: File, poster: File, remotePath: String): LocalSong = {
    require(file != null)
    require(file.exists, file + " doesn't exist")
    require(file.isDirectory.isFalse, file + " is a directory")
    val audioFile = AudioFileIO.read(file)
    val (tag, header) = audioFile.mapTo(e => (e.getTag, e.getAudioHeader))
    val year = try {
      ".*(\\d{4}).*".r.findAllIn(tag.getFirst(FieldKey.YEAR)).matchData.next().group(1).toInt
    } catch {
      case _: MatchError =>
        println(s"No year in $audioFile")
        0 // Some songs, e.g., classical, don't have a year yet.
    }
    val discNumber = Option(tag.getFirst(FieldKey.DISC_NO)).map(_.trim).filterNot(_.isEmpty)
    def parseReplayGain(s: String): String = s.dropAfterLast('=').drop(1).takeWhile(_ != '"')
    // in flac files, REPLAYGAIN_TRACK_GAIN works. In regular files, it doesn't so it needs to be parsed manually :\
    val trackGain = (tag.getFields("REPLAYGAIN_TRACK_GAIN").asScala.headOption map (_.toString))
        .orElse(tag.getFields("TXXX").asScala map (_.toString) find (_ contains "track_gain") map parseReplayGain)
        .map(_.split(' ').apply(0).toDouble) // handle the case of "1.43 dB"

    LocalSong(
      title = tag.getFirst(FieldKey.TITLE),
      artistName = tag.getFirst(FieldKey.ARTIST),
      albumName = tag.getFirst(FieldKey.ALBUM),
      track = tag.getFirst(FieldKey.TRACK).toInt,
      year = year,
      bitrate = header.getBitRate,
      duration = header.getTrackLength,
      size = file.length,
      //discNumber = discNumber,
      trackGain = trackGain,
      localPosterPath = poster,
      remotePath = remotePath,
      file = file,
    )
  }
}
