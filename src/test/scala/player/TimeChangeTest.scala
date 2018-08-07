package player

import common.AuxSpecs
import org.scalatest.FreeSpec

class TimeChangeTest extends FreeSpec with AuxSpecs {
  "fromMicroSecond" - {
    "seconds" in {TimeChange.fromMicrosecond(1e6.toInt) shouldReturn TimeChange(0, 0, 1)}
    "minutes" in {TimeChange.fromMicrosecond((1e6 * (60 * 5 + 23)).toInt) shouldReturn TimeChange(0, 5, 23)}
    "hours" in {TimeChange.fromMicrosecond(1e6.toLong * (3600 * 2 + 60 * 42 + 56)) shouldReturn TimeChange(2, 42, 56)}
  }
}
