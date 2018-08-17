package player

import rx.lang.scala.Observable
import scalaz.concurrent.Task

/** The main API of this package, controls all aspects of the player. */
trait MutablePlayer {
  private def ifInitialized[A](a: => A): A =
    if (isEmpty) throw new IllegalStateException("Uninitialized playlist")
    else a
  /** By default, all observations run on IOPool. Subscriptions aren't because of bugs in RxScala, probably. */
  def events: Observable[PlayerEvent]
  def playlist: Playlist

  /** Stops the player as well. */
  def setIndex(index: Int): Task[Unit]
  /** Resumes if paused. */
  def playCurrentSong: Task[Unit]
  def add(s: Song): Task[Unit]
  def songs: Seq[Song] = playlist.songs
  def currentSong: Song = ifInitialized(songs(currentIndex))

  def isEmpty: Boolean = playlist.isEmpty
  def size: Int = playlist.size
  def currentIndex: Int = ifInitialized(playlist.currentIndex)

  def isLastSong: Boolean = ifInitialized(playlist.isLastSong)
  def isFirstSong: Boolean = ifInitialized(playlist.isFirstSong)

  def stop: Task[Unit]
  def pause: Task[Unit]
  /** Will try to append a new song if at the end of the playlist. */
  def next: Task[Unit]
  def previous: Task[Unit]

  def status: PlayerStatus
  def isPaused: Boolean = status == Paused
  def isPlaying: Boolean = status == Playing
  def isStopped: Boolean = status == Stopped

  def setVolume(d: Double): Task[Unit]
}
