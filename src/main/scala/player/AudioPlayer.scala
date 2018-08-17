package player

import rx.lang.scala.Observable
import scalaz.concurrent.Task

/** Low level API for playing audio. */
trait AudioPlayer {
  def setSource(s: Song): Task[Unit]
  def source: Song

  def setVolume(f: Double): Task[Unit]
  def isPaused: Boolean = status == Paused
  def isPlaying: Boolean = status == Playing
  def isStopped: Boolean = status == Stopped
  def play: Task[Unit]
  def pause: Task[Unit]
  def togglePause: Task[Unit] = if (isPaused) play else pause
  def stop: Task[Unit]

  def events: Observable[AudioPlayerEvent]
  def status: PlayerStatus
}
