package player.pkg

import java.io.{FileOutputStream, InputStream}
import java.nio.file.Path
import java.util.zip.ZipInputStream

private object Unzipper {
  def unzip(zipFile: InputStream, destination: Path): Unit = {
    val zis = new ZipInputStream(zipFile)

    Stream.continually(zis.getNextEntry).takeWhile(_ != null).foreach {file =>
      if (file.isDirectory)
        return
      val outPath = destination.resolve(file.getName)
      val outPathParent = outPath.getParent
      if (!outPathParent.toFile.exists())
        outPathParent.toFile.mkdirs()

      val outFile = outPath.toFile
      val out = new FileOutputStream(outFile)
      val buffer = new Array[Byte](4096)
      Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(out.write(buffer, 0, _))
    }
  }
}