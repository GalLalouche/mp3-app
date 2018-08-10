package player

import java.util.Collections

import comm.{Communicator, ObservableSpecs}
import main.java.goxr3plus.javastreamplayer.stream.{Status, StreamPlayer, StreamPlayerEvent, StreamPlayerListener}
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
      testObservableFirstValue(player.events.select[SongChanged])(player.setSource(song))(
        _.newSong shouldReturn song)
    }

    // TODO generalize
    def onListen(f: StreamPlayerListener => Unit): Unit = {
      val captor = ArgumentCaptor.forClass(classOf[StreamPlayerListener])
      verify(sp).addStreamPlayerListener(captor.capture)
      f(captor.getValue)
    }
    "Time change events" in {
      assertMinimumEvents(player.events.select[TimeChange], 3)(onListen(l => {
        l.progress(0, 2e6.toInt, Array(), Collections.emptyMap())
        l.progress(0, 1e6.toInt, Array(), Collections.emptyMap())
        l.progress(0, (1e6 * (3600 + (60 * 42) + 56)).toInt, Array(), Collections.emptyMap())
      }))
    }

    def checkSingleEventPropagation[E <: PlayerEvent : Manifest](s: Status): Unit =
      assertMinimumEvents(player.events.select[E], 1)(
        onListen(_.statusUpdated(new StreamPlayerEvent(null, s, 0, null))))
    "Stop" in checkSingleEventPropagation[PlayerStopped](Status.STOPPED)
    "Pause" in checkSingleEventPropagation[PlayerPaused](Status.PAUSED)
    "Play" in checkSingleEventPropagation[PlayerPlaying](Status.PLAYING)
    "Resume" in checkSingleEventPropagation[PlayerPlaying](Status.RESUMED)
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
