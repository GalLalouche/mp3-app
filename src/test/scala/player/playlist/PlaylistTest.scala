package player.playlist

import comm.ObservableSpecs
import common.Regression._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import player.{Player, Song, playlist}
import scalaz.concurrent.Task

class PlaylistTest extends FreeSpec with ObservableSpecs with OneInstancePerTest with MockitoSugar {
  private val player = mock[Player]
  when(player.setSource(any())) thenReturn Task.now(())
  when(player.stop) thenReturn Task.now(())
  when(player.play) thenReturn Task.now(())
  private val $: Playlist = new Playlist.From(player)
  private val currentChangedEvents = $.events.select[CurrentChanged]
  private val song1 = mock[Song]
  private val song2 = mock[Song]
  private val song3 = mock[Song]
  private def init(): Unit = {
    ($.add(song1) >> $.add(song2) >> $.add(song3) >> $.next).unsafePerformSync
  }

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
      testObservableFirstValue(currentChangedEvents) {
        $.next.unsafePerformSync
      }(_ shouldReturn CurrentChanged(song2, 1))
    }
  }

  "previous" - {
    "retreats currentIndex" in {
      $.add(mock[Song]).unsafePerformSync
      $.add(mock[Song]).unsafePerformSync
      $.add(mock[Song]).unsafePerformSync
      $.setIndex(2).unsafePerformSync
      $.previous.unsafePerformSync
    }
    "bind pipe" taggedAs regression("Incorrect if condition in for comprehension") in {
      val song = mock[Song]
      noException shouldBe thrownBy {
        ($.add(song) >> $.playCurrentSong >> $.add(song) >> $.next >> $.previous).unsafePerformSync
      }
    }
    "throws if no previous" in {
      $.add(mock[Song]).unsafePerformSync
      $.add(mock[Song]).unsafePerformSync
      an[IllegalStateException] shouldBe thrownBy {$.previous.unsafePerformSync}
    }
    "Emits SongChanged event" in {
      val song = mock[Song]
      $.add(song).unsafePerformSync
      val song2 = mock[Song]
      $.add(song2).unsafePerformSync
      $.next.unsafePerformSync
      testObservableFirstValue(currentChangedEvents) {
        $.previous.unsafePerformSync
      }(_ shouldReturn CurrentChanged(song, 0))
    }
  }

  "setIndex" - {
    "throws on invalid index" in {
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(-1)}
      $.add(mock[Song])
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(1)}
    }
    "sets the index when valid" in {
      init()

      $.setIndex(1).unsafePerformSync
      $.currentIndex shouldReturn 1
      $.songs(1) shouldReturn song2
    }
    "emits an event when done" in {
      init()
      testObservableFirstValue(currentChangedEvents) {
        $.setIndex(1).unsafePerformSync
      }(_ shouldReturn CurrentChanged(song2, 1))
    }
  }

  "removeIndex" - {
    "throws on invalid index" in {
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(-1)}
      $.add(mock[Song])
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(1)}
    }
    "valid index" - {
      "Not current song" - {
        "After currentSong" in {
          init()
          $.size shouldReturn 3
          $.removeIndex(2).unsafePerformSync
          $.currentIndex shouldReturn 1
          $.songs shouldReturn Seq(song1, song2)
        }
        "Before current song" in {
          init()
          $.removeIndex(0).unsafePerformSync
          $.songs shouldReturn Seq(song2, song3)
          $.currentIndex shouldReturn 0
        }
      }
      "currentSong" - {
        "Can go forward" in {
          init()
          $.removeIndex(1).unsafePerformSync
          $.currentIndex shouldReturn 1
          $.songs shouldReturn Seq(song1, song3)
        }
        "Can go backwards" in {
          init()
          $.removeIndex(1).unsafePerformSync
          $.removeIndex(1).unsafePerformSync
          $.currentIndex shouldReturn 0
          $.songs shouldReturn Seq(song1)
        }
      }
      "events" - {
        "after currentSong" in {
          init()
          testObservableFirstValue($.events.select[SongRemoved]) {
            $.removeIndex(2).unsafePerformSync
          }(_ shouldReturn SongRemoved(song3, 2))
        }
        "before currentSong" in {
          init()
          testObservableFirstValue($.events.select[SongRemoved]) {
            $.removeIndex(0).unsafePerformSync
          }(_ shouldReturn SongRemoved(song1, 0))
        }
        "currentSong" in {
          init()
          testExpectedEvents($.events, exact = true) {
            $.removeIndex(1).unsafePerformSync
          }(Seq(SongRemoved(song2, 1), playlist.CurrentChanged(song3, 2)))
        }
      }
    }
  }
}
