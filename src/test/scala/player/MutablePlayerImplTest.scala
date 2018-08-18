package player

import comm.ObservableSpecs
import common.Regression.regression
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import player.MutablePlayerImplTest.FakePlayer
import player.pkg.PackagedAlbum
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.ToFunctorOps

class MutablePlayerImplTest extends FreeSpec with ObservableSpecs with MockitoSugar with OneInstancePerTest {
  private val audioPlayer = spy(new FakePlayer)
  private val songFetcher = mock[SongFetcher]
  private val $ = new MutablePlayerImpl(
    audioPlayer,
    UpdatablePlaylist.empty, // No reason to mock it since it's a pure data class
    songFetcher,
  )

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
      when(audioPlayer.status).thenReturn(Stopped)
      $.add(song).unsafePerformSync
      $.playCurrentSong.unsafePerformSync
      verify(audioPlayer).play
    }
  }

  "add" - {
    "when first: emits a current changed event and sets source (but only after the song was added!)" in {
      val song = mock[Song]
      testExpectedEvents($.events)($.add(song))(Seq(SongAdded(song, 0), CurrentChanged(song, 0)))
    }
    "increases size" in {
      $.add(mock[Song]).unsafePerformSync
      $.size shouldReturn 1
      $.currentIndex shouldReturn 0
    }
    "Emits SongAdded event" in {
      val song = mock[Song]
      testObservableFirstValue($.events.select[SongAdded])($.add(song))(_ shouldReturn SongAdded(song, 0))
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
        ($.add(song) >> $.playCurrentSong >> $.add(song) >> $.next).unsafePerformSync
      }
    }
    "fetches if no next" in {
      $.add(mock[Song]).unsafePerformSync
      val fetchedSong = mock[Song]
      when(songFetcher.apply) thenReturn Task.now(fetchedSong)
      $.next.unsafePerformSync
      $.currentSong shouldReturn fetchedSong
      $.currentIndex shouldReturn 1
    }
    "Emits SongChanged event" in {
      val song = mock[Song]
      $.add(song).unsafePerformSync
      val song2 = mock[Song]
      $.add(song2).unsafePerformSync
      testObservableFirstValue($.events.select[CurrentChanged])($.next)(_ shouldReturn CurrentChanged(song2, 1))
    }

    "Multiple songs" in {
      val firstSong = mock[LocalSong]
      $.add(PackagedAlbum(Seq(firstSong, mock[LocalSong]))).unsafePerformSync
      $.currentIndex shouldReturn 0
      audioPlayer.source shouldReturn firstSong
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
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.previous.unsafePerformSync}
    }
    "Emits SongChanged event" in {
      val song = mock[Song]
      $.add(song).unsafePerformSync
      val song2 = mock[Song]
      $.add(song2).unsafePerformSync
      $.next.unsafePerformSync
      testObservableFirstValue($.events.select[CurrentChanged])($.previous)(_ shouldReturn CurrentChanged(song, 0))
    }
  }

  "setIndex" - {
    "throws on invalid index" in {
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(-1)}
      $.add(mock[Song]).unsafePerformSync
      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(1).unsafePerformSync}
    }
    "sets the index when valid" in {
      init()

      $.setIndex(1).unsafePerformSync
      $.currentIndex shouldReturn 1
      $.songs(1) shouldReturn song2
    }
    "emits an event when done" in {
      init()
      testObservableFirstValue($.events.select[CurrentChanged])($.setIndex(1))(_ shouldReturn CurrentChanged(song2, 1))
    }
  }
  //
  //  "removeIndex" - {
  //    "throws on invalid index" in {
  //      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(-1)}
  //      $.add(mock[Song])
  //      an[IndexOutOfBoundsException] shouldBe thrownBy {$.setIndex(1)}
  //    }
  //    "valid index" - {
  //      "Not current song" - {
  //        "After currentSong" in {
  //          init()
  //          $.size shouldReturn 3
  //          $.removeIndex(2).unsafePerformSync
  //          $.currentIndex shouldReturn 1
  //          $.songs shouldReturn Seq(song1, song2)
  //        }
  //        "Before current song" in {
  //          init()
  //          $.removeIndex(0).unsafePerformSync
  //          $.songs shouldReturn Seq(song2, song3)
  //          $.currentIndex shouldReturn 0
  //        }
  //      }
  //      "currentSong" - {
  //        "Can go forward" in {
  //          init()
  //          $.removeIndex(1).unsafePerformSync
  //          $.currentIndex shouldReturn 1
  //          $.songs shouldReturn Seq(song1, song3)
  //        }
  //        "Can go backwards" in {
  //          init()
  //          $.removeIndex(1).unsafePerformSync
  //          $.removeIndex(1).unsafePerformSync
  //          $.currentIndex shouldReturn 0
  //          $.songs shouldReturn Seq(song1)
  //        }
  //      }
  //      "events" - {
  //        "after currentSong" in {
  //          init()
  //          testObservableFirstValue($.events.select[SongRemoved])($.removeIndex(2))(
  //            _ shouldReturn SongRemoved(song3, 2))
  //        }
  //        "before currentSong" in {
  //          init()
  //          testObservableFirstValue($.events.select[SongRemoved])($.removeIndex(0))(
  //            _ shouldReturn SongRemoved(song1, 0))
  //        }
  //        "currentSong" in {
  //          init()
  //          testExpectedEvents($.events, exact = true)($.removeIndex(1))(
  //            Seq(SongRemoved(song2, 1), playlist.CurrentChanged(song3, 2)))
  //        }
  //      }
  //    }
  //  }
  //}
}

object MutablePlayerImplTest {
  private class FakePlayer extends AudioPlayer with ToFunctorOps {
    var source: Song = _
    private val voidTask: Task[Unit] = Task.now(())
    override def setSource(s: Song): Task[Unit] = Task(source = s).void
    override def setVolume(f: Double): Task[Unit] = voidTask
    override def play: Task[Unit] = {
      assert(source != null)
      Task(status = Playing).void
    }
    override def pause: Task[Unit] = Task(status = Paused).void
    override def stop: Task[Unit] = Task(status = Stopped).void
    override def events: Observable[PlayerEvent] = Subject()
    var status: PlayerStatus = _
  }
}
