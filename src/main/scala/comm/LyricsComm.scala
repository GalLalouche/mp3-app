package comm

import javax.inject.Inject
import player.Song
import scalaz.concurrent.Task

trait LyricsComm {
  def apply(s: Song): Task[Lyrics]
}

object LyricsComm {
  class From @Inject() private[comm](c: Communicator) extends LyricsComm {
    override def apply(s: Song): Task[Lyrics] = c.getString("lyrics/" + s.remotePath).map(l => {
      if (l.contains("assets/images/TrebleClef.png")) Instrumental else WordLyrics("<html>" + l + "</html>")
    })
  }
}
