package player.playlist

import common.rich.func.{ToMoreApplicativeOps, ToMoreFunctorOps}
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
    with ToMoreFunctorOps with ToMoreApplicativeOps {
  class From @Inject()(override val player: Player) extends Playlist {
    override val songs = new mutable.ArrayBuffer[Song]()
    private var _currentIndex = -1
    override def currentIndex = _currentIndex
    override def playCurrentSong: Task[Unit] =
      if (_currentIndex < 0)
        throw new IllegalStateException("Requested play current song before any songs were added")
      else
        player.setSource(songs(currentIndex)) >> player.stop >> player.play
    override def add(s: Song): Task[Unit] = {
      songs += s
      for {
        _ <- Task(observable.onNext(SongAdded(s, songs.size - 1)))
        _ <- next if _currentIndex < 0
      } yield ()
    }
    override def size: Int = songs.size
    private val observable = Subject[PlaylistEvent]()
    override def events: Observable[PlaylistEvent] = observable
    override def stop = player.stop
    override def playOrPause = player.playOrPause

    override def next = {
      val t = Task {
        if (lastSong)
          throw new IllegalStateException("Request next but playlist is already at last song")
        _currentIndex += 1
        assert(currentIndex <= size - 1)
        player.isPlaying
      }

      for {
        wasPlaying <- t
        _ <- Task(observable.onNext(CurrentChanged(currentSong, currentIndex)))
        _ <- if (wasPlaying) playCurrentSong else player.setSource(currentSong)
      } yield ()
    }
  }
}
