package comm

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream

import javax.imageio.ImageIO
import javax.inject.Inject
import player.Song
import scalaz.concurrent.Task

trait PosterComm {
  def poster(s: Song): Task[BufferedImage]
}
object PosterComm {
  class From @Inject()(communicator: Communicator) extends PosterComm {
    override def poster(s: Song): Task[BufferedImage] =
      communicator.getBytes(s.poster.replaceAll(" ", "%20"))
          .map(bytes => ImageIO.read(new ByteArrayInputStream(bytes)).ensuring(_ != null))
  }
}
