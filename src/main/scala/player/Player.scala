package player

import rx.lang.scala.Observable
import scalaz.concurrent.Task

trait Player {

  def setSource(s: Song): Task[Unit]
  def setVolume(f: Double): Task[Unit]
  def isPaused: Boolean
  def isPlaying: Boolean
  def isStopped: Boolean
  def play: Task[Unit]
  def pause: Task[Unit]
  def togglePause: Task[Unit] = if (isPaused) play else pause
  def stop: Task[Unit]

  def events: Observable[PlayerEvent]
}
