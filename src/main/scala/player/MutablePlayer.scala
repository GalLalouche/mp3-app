package player

import rx.lang.scala.Observable
import scalaz.concurrent.Task

/** The main API of this package, controls all aspects of the player. */
trait MutablePlayer {
  /** By default, all subscriptions and observations run on IOPool. */
  def events: Observable[PlayerEvent]
  def playlist: Playlist

  /** Stops the player as well. */
  def setIndex(index: Int): Task[Unit]
  /** Resumes if paused. */
  def playCurrentSong: Task[Unit]
  def add(s: Song): Task[Unit]
  def songs: Seq[Song] = playlist.songs
  def currentSong = songs(currentIndex)

  def isEmpty: Boolean = playlist.isEmpty
  def size: Int = playlist.size
  def currentIndex: Int = playlist.currentIndex

  def isLastSong: Boolean = playlist.isLastSong
  def isFirstSong: Boolean = playlist.isFirstSong

  def stop: Task[Unit]
  /** Will try to append a new song if at the end of the playlist. */
  def next: Task[Unit]
  def previous: Task[Unit]

  def status: PlayerStatus
}
