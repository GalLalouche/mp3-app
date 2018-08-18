package comm

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import javax.imageio.ImageIO
import javax.inject.Inject
import player.{LocalSong, RemoteSong, Song}
import scalaz.concurrent.Task

trait PosterComm {
  def poster(s: Song): Task[BufferedImage]
}
object PosterComm {
  class From @Inject() private[comm](communicator: Communicator) extends PosterComm {
    override def poster(s: Song): Task[BufferedImage] = s match {
      case e: LocalSong => Task(ImageIO.read(e.localPosterPath))
      case e: RemoteSong =>
        communicator.getBytes(e.poster.replaceAll(" ", "%20"))
            .map(bytes => ImageIO.read(new ByteArrayInputStream(bytes)).ensuring(_ != null))
    }
  }
}
