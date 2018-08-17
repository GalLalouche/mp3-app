package comm

import javax.inject.Inject
import player.{RemoteSong, Song, SongFetcher}
import scalaz.concurrent.Task

trait RandomSong extends SongFetcher {
  override def apply: Task[Song]
}

object RandomSong {
  class From @Inject()(c: Communicator) extends RandomSong {
    override def apply: Task[Song] = c.parseJson[RemoteSong]("data/randomSong/flac")
  }
}
