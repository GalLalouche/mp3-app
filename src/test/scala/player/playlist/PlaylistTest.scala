package player.playlist

import comm.ObservableSpecs
import common.Regression._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import player.{Player, Song}
import scalaz.concurrent.Task

class PlaylistTest extends FreeSpec with ObservableSpecs with OneInstancePerTest with MockitoSugar {
  private val player = mock[Player]
  when(player.setSource(any())) thenReturn Task.now(())
  when(player.stop) thenReturn Task.now(())
  when(player.play) thenReturn Task.now(())
  private val $ = new Playlist.From(player)

  "playCurrentSong" - {
    "throw initially" in {
      an[IllegalStateException] shouldBe thrownBy {$.playCurrentSong.unsafePerformSync}
    }
    "plays after song added" in {
      val song = mock[Song]
      $.add(song).unsafePerformSync
      $.playCurrentSong.unsafePerformSync
      verify(player).play
    }
  }

  "add" - {
    "increases size" in {
      $.add(mock[Song]).unsafePerformSync
      $.size shouldReturn 1
      $.currentIndex shouldReturn 0
    }
    "Emits SongAdded event" in {
      val song = mock[Song]
      testObservableFirstValue($.events.select[SongAdded]) {
        $.add(song).unsafePerformSync
      }(_ shouldReturn SongAdded(song, 0))
    }
  }

  "next" - {
    "advanced currentIndex" in {
      $.add(mock[Song]).unsafePerformSync
      $.add(mock[Song]).unsafePerformSync
      $.add(mock[Song]).unsafePerformSync
      $.size shouldReturn 3
      $.currentIndex shouldReturn 0
      $.next.unsafePerformSync
      $.currentIndex shouldReturn 1
      $.next.unsafePerformSync
      $.currentIndex shouldReturn 2
    }
    "bind pipe" taggedAs regression("Incorrect if condition in for comprehension") in {
      val song = mock[Song]
      noException shouldBe thrownBy {
        $.add(song).>>($.playCurrentSong).>>($.add(song)).>>($.next).unsafePerformSync
      }
    }
    "throws if no next" in {
      $.add(mock[Song])
      $.next.unsafePerformSync
      an[IllegalStateException] shouldBe thrownBy {$.next.unsafePerformSync}
    }
    "Emits SongChanged event" in {
      val song = mock[Song]
      $.add(song).unsafePerformSync
      val song2 = mock[Song]
      $.add(song2).unsafePerformSync
      testObservableFirstValue($.events.select[CurrentChanged]) {
        $.next.unsafePerformSync
      }(_ shouldReturn CurrentChanged(song2, 1))
    }
  }
}
