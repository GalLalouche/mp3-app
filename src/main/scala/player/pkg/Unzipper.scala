package player.pkg

import java.io.{FileOutputStream, InputStream}
import java.nio.file.Path

import common.rich.collections.RichTraversableOnce._
import java.util.zip.ZipInputStream

import common.rich.path.{Directory, RichFileUtils}

private object Unzipper {
  def unzip(zipFile: InputStream, destination: Path): Unit = {
    val zis = new ZipInputStream(zipFile)

    Stream.continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .filterNot(_.isDirectory)
        .foreach {file =>
          val outPath = destination.resolve(file.getName)
          val outPathParent = outPath.getParent
          if (!outPathParent.toFile.exists())
            outPathParent.toFile.mkdirs()

          val outFile = outPath.toFile
          val out = new FileOutputStream(outFile)
          val buffer = new Array[Byte](4096)
          Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(out.write(buffer, 0, _))
        }

    val dir = Directory(destination.toFile)
    if (dir.dirs.nonEmpty) { // Invalid format: zip contains a dir with all files.
      assert(dir.files.isEmpty)
      val subDir = dir.dirs.single
      RichFileUtils.moveContents(subDir, dir)
      subDir.deleteAll()
    }
  }
}
