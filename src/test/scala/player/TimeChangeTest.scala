package player

import common.AuxSpecs
import org.scalatest.FreeSpec
import common.rich.RichT._

class TimeChangeTest extends FreeSpec with AuxSpecs {
  "fromMicroSecond" - {
    def fromMicroseconds(d: Double)(hours: Int, minutes: Int, seconds: Int): Unit =
      TimeChange.apply(d.toLong, Long.MaxValue).toTuple(_.hours, _.minutes, _.seconds) shouldReturn (hours, minutes, seconds)
    "seconds" in fromMicroseconds(1e6)(0,0,1)
    "minutes" in fromMicroseconds((1e6 * (60 * 5 + 23)).toInt)(0, 5, 23)
    "hours" in fromMicroseconds(1e6.toLong * (3600 * 2 + 60 * 42 + 56))(2, 42, 56)
  }
}
