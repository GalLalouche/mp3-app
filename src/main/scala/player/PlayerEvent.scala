package player

import java.time.Duration

sealed trait PlayerEvent
case class SongAdded(s: Song, index: Int) extends PlayerEvent
case class CurrentChanged(s: Song, index: Int) extends PlayerEvent
case class SongRemoved(s: Song, index: Int) extends PlayerEvent
case class StatusChanged(newStatus: PlayerStatus) extends PlayerEvent

object SongFinished extends PlayerEvent
object PlayerStopped extends PlayerEvent
object PlayerPaused extends PlayerEvent
object PlayerPlaying extends PlayerEvent

class TimeChange(private val currentTimeInMicroSeconds: Long, totalTimeInMicroSeconds: Long) extends PlayerEvent {
  def diffFrom(other: TimeChange): Duration =
    Duration.ofMillis((currentTimeInMicroSeconds - other.currentTimeInMicroSeconds) * 1000)

  require(currentTimeInMicroSeconds <= totalTimeInMicroSeconds)
  require(currentTimeInMicroSeconds >= 0)
  val inSeconds: Int = (currentTimeInMicroSeconds / 1e6.toLong).toInt
  val seconds: Int = inSeconds % 60
  assert(seconds >= 0 && seconds <= 60)
  val minutes: Int = (inSeconds % 3600) / 60
  assert(minutes >= 0 && minutes <= 60)
  val hours: Int = inSeconds / 3600
  assert(hours >= 0)

  def toDoubleDigitDisplay: String = {
    def aux(n: Int): String = {
      assert(n <= 100)
      assert(n >= 0)
      "%02d".format(n)
    }

    if (hours == 0) s"$minutes:${aux(seconds)}"
    else s"$hours:${aux(minutes)}:${aux(seconds)}"
  }

  /** A number between 0 and 100. */
  def percentage: Double = currentTimeInMicroSeconds / totalTimeInMicroSeconds.toDouble
}
object TimeChange {
  val empty: TimeChange = TimeChange(0, 0)

  def apply(currentTimeInMicroSeconds: Long, totalTimeInMicroSeconds: Long): TimeChange =
    new TimeChange(currentTimeInMicroSeconds, totalTimeInMicroSeconds)
}
