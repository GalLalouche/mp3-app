package player

import java.util.Collections
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import comm.Communicator
import common.AuxSpecs
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import main.java.goxr3plus.javastreamplayer.stream.{StreamPlayer, StreamPlayerListener}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Matchers}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import rx.lang.scala.Observable
import scalaz.syntax.ToBindOps

class StreamPlayerWrapperTest extends FreeSpec with AuxSpecs with MockitoSugar with OneInstancePerTest
    with ToMoreMonadPlusOps with ToBindOps with MoreObservableInstances {
  private val sp = mock[StreamPlayer]
  private val player = {
    val c = mock[Communicator]
    when(c.path(Matchers.anyString)).thenReturn("http://localhost")
    new StreamPlayerWrapper(c, sp)
  }

  // TODO Move to AuxSpecs
  private def testObservableMultipleValues[A](o: Observable[A], expectedValues: Int)(f: => Any
  )(assertionOnFirst: Seq[A] => Unit): Unit = {
    val blockingQueue = new LinkedBlockingQueue[A]
    o.doOnNext(blockingQueue.put).subscribe()
    f

    val results = for (_ <- 1 to expectedValues) yield {
      val $ = blockingQueue.poll(1, TimeUnit.SECONDS)
      if ($ == null)
      // TODO smarter stack trace
        throw new TestFailedException("Blocking queue did not return a result", 3)
      $
    }
    assertionOnFirst(results)
  }
  private def testObservableFirstValue[A](o: Observable[A])(f: => Any)(assertionOnFirst: A => Unit): Unit =
    testObservableMultipleValues(o, 1)(f)(e => assertionOnFirst(e.head))
  private def assertMinimumEvents(o: Observable[_], expectedValues: Int)(f: => Any): Unit =
    testObservableMultipleValues(o, expectedValues)(f)(_ => ())

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
        listener.progress(0, 1e6.toInt, Array(), Collections.emptyMap())
        listener.progress(0, 2e6.toInt, Array(), Collections.emptyMap())
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
