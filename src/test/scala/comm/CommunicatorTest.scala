package comm

import common.AuxSpecs
import org.scalatest.FreeSpec
import org.scalatest.mockito.MockitoSugar

class CommunicatorTest extends FreeSpec with AuxSpecs with MockitoSugar {
  "path" in {
    val talker = mock[InternetTalker]
    val c = new Communicator.From(talker, "http://localhost:9000")
    c.path("foo") shouldReturn "http://localhost:9000/foo"
    c.path("/foo") shouldReturn "http://localhost:9000/foo"
  }
}
