package player.playlist

import common.rich.func.{ToMoreApplicativeOps, ToMoreFunctorOps}
import javax.inject.Inject
import player.{AudioPlayer, CurrentChanged, PlayerEvent, Song, SongAdded, SongRemoved}
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.ToBindOps

import scala.collection.mutable

trait Playlist {
  def player: AudioPlayer

  def setIndex(index: Int): Task[Unit]
  def removeIndex(index: Int): Task[Unit]
  def playCurrentSong: Task[Unit]
  def add(s: Song): Task[Unit]
  def songs: Seq[Song]
  def currentSong = songs(currentIndex)

  def isEmpty: Boolean = size == 0
  def size: Int = songs.size
  def currentIndex: Int

  def isLastSong: Boolean = currentIndex == size - 1
  def isFirstSong: Boolean = currentIndex == 0

  def events: Observable[PlayerEvent]
  def stop: Task[Unit]

  def next: Task[Unit]
  def previous: Task[Unit]
}

object Playlist extends ToBindOps
    with ToMoreFunctorOps with ToMoreApplicativeOps {
  class From @Inject()(override val player: AudioPlayer) extends Playlist {
    override val songs = new mutable.ArrayBuffer[Song]()
    private var _currentIndex = -1
    override def currentIndex = _currentIndex
    private def checkIndex(i: Int): Unit =
      if (i >= size || i < 0)
        throw new IndexOutOfBoundsException(s"Invalid song index <$i>; total size is <$size>")
    private def emitCurrentChanged: Unit =
      observable.onNext(CurrentChanged(currentSong, _currentIndex))
    override def setIndex(i: Int): Task[Unit] = {
      checkIndex(i)
      Task {
        _currentIndex = i
        emitCurrentChanged
      }
    }

    override def removeIndex(i: Int): Task[Unit] = {
      checkIndex(i)
      if (size == 1)
        throw new UnsupportedOperationException("No support for yet for removing a file without replacement")
      val songRemoved = SongRemoved(songs(i), i)
      if (currentIndex == i) (if (isLastSong) previous else next) >> removeIndex(i)
      else
        Task {
          songs.remove(i)
          if (i < currentIndex)
            _currentIndex -= 1
        } >| observable.onNext(songRemoved)
    }
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
    private val observable = Subject[PlayerEvent]()
    override def events: Observable[PlayerEvent] = observable
    override def stop = player.stop

    override def next = {
      val t = Task {
        if (isLastSong)
          throw new IllegalStateException("Request next but playlist is already at last song")
        _currentIndex += 1
        assert(currentIndex <= size - 1)
        player.isPlaying
      }

      for {
        wasPlaying <- t
        _ = emitCurrentChanged
        _ <- if (wasPlaying) playCurrentSong else player.setSource(currentSong)
      } yield ()
    }
    override def previous = {
      val t = Task {
        if (isFirstSong)
          throw new IllegalStateException("Request previous but playlist is already at first song")
        _currentIndex -= 1
        assert(currentIndex >= 0)
        player.isPlaying
      }

      for {
        wasPlaying <- t
        _ = emitCurrentChanged
        _ <- if (wasPlaying) playCurrentSong else player.setSource(currentSong)
      } yield ()
    }
  }
}
