package player

import java.util.Collections

import comm.{Communicator, ObservableSpecs}
import common.RichTask._
import main.java.goxr3plus.javastreamplayer.stream.{StreamPlayer, StreamPlayerListener}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

class StreamPlayerWrapperTest extends FreeSpec with ObservableSpecs
    with MockitoSugar with OneInstancePerTest {
  private val sp = mock[StreamPlayer]
  private val player = {
    val c = mock[Communicator]
    when(c.path(Matchers.anyString)).thenReturn("http://localhost")
    new StreamPlayerWrapper(c, sp)
  }

  "Events" - {
    "setSource emits a SongChanged event" in {
      val song = mock[Song]
      testObservableFirstValue(player.events.select[SongChanged]) {
        player.setSource(song).fireAndForget()
      }(_.newSong shouldReturn song)
    }

    "Time change events" in {
      assertMinimumEvents(player.events.select[TimeChange], 3) {
        // TODO generalize
        val captor = ArgumentCaptor.forClass(classOf[StreamPlayerListener])
        verify(sp).addStreamPlayerListener(captor.capture)
        val listener = captor.getValue
        listener.progress(0, 2e6.toInt, Array(), Collections.emptyMap())
        listener.progress(0, 1e6.toInt, Array(), Collections.emptyMap())
        listener.progress(0, (1e6 * (3600 + (60 * 42) + 56)).toInt, Array(), Collections.emptyMap())
      }
    }
  }

  "setVolume" - {
    "passes volume before playing" in {
      player.setVolume(0.5).unsafePerformSync
      // First volume is set, in case sp is playing
      verify(sp).setGain(0.5)
      player.setSource(mock[Song]).>>(player.play).unsafePerformSync
      // Set again, because sp is stupid
      verify(sp, times(2)).setGain(0.5)
    }
  }
}
