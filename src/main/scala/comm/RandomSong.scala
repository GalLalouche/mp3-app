package comm

import javax.inject.Inject
import player.{RemoteSong, Song}
import scalaz.concurrent.Task
import spray.json._

trait RandomSong {
  def randomSong: Task[Song]
}

object RandomSong {
  class From @Inject()(c: Communicator) extends RandomSong {
    override def randomSong: Task[Song] = c.getString("data/randomSong/flac").map(_.parseJson.convertTo[RemoteSong])
  }
}
