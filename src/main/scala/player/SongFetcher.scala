package player

import scalaz.concurrent.Task

trait SongFetcher {
  def apply: Task[Song]
}
