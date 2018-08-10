package player

sealed trait PlayerEvent

case class TimeChange(hours: Int, minutes: Int, seconds: Int) extends PlayerEvent {
  require(hours >= 0)
  require(minutes >= 0 && minutes <= 60)
  require(seconds >= 0 && seconds <= 60)

  def toDoubleDigitDisplay: String = {
    def aux(n: Int): String = {
      assert(n <= 100)
      assert(n >= 0)
      "%02d".format(n)
    }

    if (hours == 0) s"$minutes:${aux(seconds)}"
    else s"$hours:${aux(minutes)}:${aux(seconds)}"
  }
}
object TimeChange {
  def fromMicrosecond(microsecondPosition: Long): TimeChange = {
    require(microsecondPosition >= 0)
    val inSeconds: Long = microsecondPosition / 1e6.toLong
    val seconds = inSeconds % 60
    assert(seconds >= 0 && seconds <= 60)
    val minutes = (inSeconds % 3600) / 60
    assert(minutes >= 0 && minutes <= 60)
    val hours = inSeconds / 3600
    assert(hours >= 0 && hours <= Integer.MAX_VALUE)
    TimeChange(hours.toInt, minutes.toInt, seconds.toInt)
  }
}

case class SongChanged(newSong: Song) extends PlayerEvent
case class PlayerStopped() extends PlayerEvent
case class PlayerPaused() extends PlayerEvent
case class PlayerPlaying() extends PlayerEvent
