package comm.external

import comm.Communicator
import javax.inject.Inject
import player.Song
import scalaz.concurrent.Task

trait ExternalLinksComm {
  def links(s: Song): Task[ExternalLinks]
}

object ExternalLinksComm {
  class From @Inject() private[comm](c: Communicator) extends ExternalLinksComm {
    override def links(s: Song): Task[ExternalLinks] = c.parseJson[ExternalLinks]("external/" + s.remotePath)
  }
}
