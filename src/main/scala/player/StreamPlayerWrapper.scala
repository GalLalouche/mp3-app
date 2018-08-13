package player

import java.net.URL

import common.rich.primitives.RichBoolean._
import java.util
import java.util.logging.{Level, Logger}

import comm.Communicator
import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import main.java.goxr3plus.javastreamplayer.stream.{Status, StreamPlayer, StreamPlayerEvent, StreamPlayerListener}
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.{ToApplicativeOps, ToBindOps}

class StreamPlayerWrapper private[player](c: Communicator, sp: StreamPlayer) extends Player
    with ToMoreFunctorOps with ToApplicativeOps with ToBindOps {
  @Inject() def this(c: Communicator) = this(c, new StreamPlayer)
  Logger.getLogger("main.java.goxr3plus").setLevel(Level.WARNING)
  def stop: Task[Unit] = Task(sp.stop()) unlessM isStopped

  private val observable = Subject[PlayerEvent]()
  private var currentSong: Song = _

  sp.addStreamPlayerListener(new StreamPlayerListener {
    override def opened(dataSource: scala.Any, properties: util.Map[String, AnyRef]): Unit = ()
    override def progress(nEncodedBytes: Int, microsecondPosition: Long, pcmData: Array[Byte], properties: util.Map[String, AnyRef]): Unit =
      observable.onNext(TimeChange(microsecondPosition, currentSong.totalLengthInMicroSeconds))
    override def statusUpdated(event: StreamPlayerEvent): Unit = {
      (event.getPlayerStatus match {
        case Status.PAUSED => Some(PlayerPaused())
        case Status.STOPPED => Some(PlayerStopped())
        case Status.PLAYING | Status.RESUMED => Some(PlayerPlaying())
        case _ => None
      }).foreach(observable.onNext)
    }
  })
  private var volume: Double = _
  private def trySetSource(s: Song): Task[Unit] = Task(try {
    s match {
      case e: LocalSong => sp.open(e.file)
      case e: RemoteSong => sp.open(new URL(c.path(e.path)))
    }
    currentSong = s
  } catch {
    case e: IllegalArgumentException =>
      println(s"Failed to set source for <$s>")
      throw e
  })

  override def setSource(s: Song): Task[Unit] =
    stop >> trySetSource(s) >| observable.onNext(SongChanged(s))
  override def play: Task[Unit] = Task {
    assert(isPlaying.isFalse)
    if (isPaused) sp.resume() else sp.play()
    sp.setGain(volume)
  } unlessM isPlaying
  override def pause: Task[Unit] = Task(sp.pause()) unlessM isPaused
  override def isPaused: Boolean = sp.isPaused
  override def isStopped: Boolean = sp.isStopped
  override def setVolume(f: Double): Task[Unit] = Task {
    require(f >= 0 && f <= 1)
    sp.setGain(f)
    volume = f
  }
  override def events: Observable[PlayerEvent] = observable
  override def isPlaying: Boolean = sp.isPlaying
}
