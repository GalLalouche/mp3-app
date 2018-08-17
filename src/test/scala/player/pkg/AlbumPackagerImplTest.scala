package player.pkg

import java.io.File

import common.rich.path.RichFile._
import common.{AuxSpecs, DirectorySpecs}
import org.scalatest.FreeSpec
import player.LocalSong

class AlbumPackagerImplTest extends FreeSpec with AuxSpecs with DirectorySpecs {
  private val $ = new AlbumPackagerImpl(tempDir)
  "unpackage" in {
    def outputFile(s: String): File = tempDir \ "pkg" \ s
    val result = $.packageFromZip(getResourceFile("pkg.zip"))
    result.songs shouldSetEqual Seq(
      LocalSong(
        title = "Foo1",
        artistName = "Bar",
        albumName = "Moo",
        track = 1,
        year = 1999,
        bitrate = "8",
        duration = 4,
        size = 5531,
        trackGain = Some(-5.0),
        file = outputFile("1.mp3"),
        localPosterPath = outputFile("folder.jpg"),
        remotePath = "localhost:1",
      ),
      LocalSong(
        title = "Foo2",
        artistName = "Bar",
        albumName = "Moo",
        track = 2,
        year = 1999,
        bitrate = "8",
        duration = 4,
        size = 5531,
        trackGain = Some(1.25),
        file = outputFile("2.mp3"),
        localPosterPath = outputFile("folder.jpg"),
        remotePath = "localhost:2",
      ),
    )
  }
}
