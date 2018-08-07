package player.playlist

import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import player.{Player, Song}
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.ToBindOps

import scala.collection.mutable

trait Playlist {

  def player: Player

  def playCurrentSong: Task[Unit]
  def add(s: Song): Task[Unit]
  def songs: Seq[Song]
  def currentSong = songs(currentIndex)

  def isEmpty: Boolean = size == 0
  def size: Int
  def currentIndex: Int

  def lastSong: Boolean = currentIndex == size - 1

  def events: Observable[PlaylistEvent]
  def stop: Task[Unit]
  def playOrPause: Task[Unit]

  def next: Task[Unit]
}

object Playlist extends ToBindOps
    with ToMoreFunctorOps {
  class From @Inject()(override val player: Player) extends Playlist {
    override val songs = new mutable.ArrayBuffer[Song]()
    private var _currentIndex = -1
    override def currentIndex = _currentIndex
    override def playCurrentSong: Task[Unit] =
      player.setSource(songs(currentIndex)) >> player.stop >> player.play
    override def add(s: Song): Task[Unit] = {
      songs += s
      for {
        _ <- Task(observable.onNext(SongAdded(s, size)))
        _ <- if (_currentIndex < 0) next else Task.now(Unit)
      } yield ()
    }
    override def size: Int = songs.size
    private val observable = Subject[PlaylistEvent]()
    override def events = observable
    override def stop = player.stop
    override def playOrPause = player.playOrPause

    override def next = {
      if (lastSong)
        throw new IllegalStateException("Request next but playlist is already at last song")
      _currentIndex += 1
      assert(currentIndex <= size - 1)
      val wasPlaying = player.isPlaying

      for {
        _ <- Task(events.onNext(CurrentChanged(currentSong, currentIndex)))
        _ <- if (wasPlaying) playCurrentSong else player.setSource(currentSong)
      } yield ()
    }
  }
}
