package player.pkg

import java.io.{File, FileInputStream}

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.path.Directory
import common.rich.path.RichFile._
import spray.json.DefaultJsonProtocol._
import spray.json._

private class AlbumPackagerImpl private[pkg](dir: Directory) extends AlbumPackager {
  def this() = this(Directory(System.getProperty("java.io.tmpdir")))
  override def packageFromZip(f: File): PackagedAlbum = {
    val outputDir = dir.addSubDir(f.nameWithoutExtension).clear()
    val fis = new FileInputStream(f)
    try {
      Unzipper.unzip(fis, outputDir.toPath)

      val json = outputDir.\("remote_paths.json").readAll.parseJson.convertTo[Map[String, JsValue]]

      val poster =
        outputDir.files.filter(e => e.nameWithoutExtension == "folder" && Set("png", "jpg")(e.extension)).single
      val songs = outputDir.files
          .filter(_.extension |> Set("mp3", "flac"))
          .map(file => SongParser(file, poster, json(file.name).asInstanceOf[JsString].value))
          .sortBy(_.track)
      PackagedAlbum(songs)
    } finally {
      fis.close()
    }
  }
}
