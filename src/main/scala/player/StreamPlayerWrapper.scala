package player

import java.net.URL
import java.util
import java.util.logging.{Level, Logger}

import comm.Communicator
import common.rich.func.ToMoreFunctorOps
import common.rich.primitives.RichBoolean._
import common.{IOPool, Percentage}
import javax.inject.Inject
import main.java.goxr3plus.javastreamplayer.stream.{Status, StreamPlayer, StreamPlayerEvent, StreamPlayerListener}
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.{ToApplicativeOps, ToBindOps}

private class StreamPlayerWrapper private[player](c: Communicator, sp: StreamPlayer) extends AudioPlayer
    with ToMoreFunctorOps with ToApplicativeOps with ToBindOps {
  @Inject() def this(c: Communicator) = this(c, new StreamPlayer)
  Logger.getLogger("main.java.goxr3plus").setLevel(Level.WARNING)
  def stop: Task[Unit] = Task(sp.stop()) unlessM isStopped

  private val observable = Subject[PlayerEvent]()
  var source: Song = _

  sp.addStreamPlayerListener(new StreamPlayerListener {
    override def opened(dataSource: scala.Any, properties: util.Map[String, AnyRef]): Unit = ()
    override def progress(nEncodedBytes: Int, microsecondPosition: Long, pcmData: Array[Byte], properties: util.Map[String, AnyRef]): Unit = {
      // microsecondPosition is bugged after skipping.
      val currentTimeInMicroseconds =
        Math.max(properties.get("mp3.position.microseconds").asInstanceOf[Long], microsecondPosition)

      val totalLengthInMicroSeconds = source.totalLengthInMicroSeconds
      if (currentTimeInMicroseconds > totalLengthInMicroSeconds) // Yey, more bugs!
        observable.onNext(SongFinished)
      else
        observable.onNext(TimeChange(currentTimeInMicroseconds, totalLengthInMicroSeconds))
    }
    override def statusUpdated(event: StreamPlayerEvent): Unit = {
      (event.getPlayerStatus match {
        case Status.PAUSED => Some(PlayerPaused)
        case Status.STOPPED => Some(PlayerStopped)
        case Status.PLAYING | Status.RESUMED => Some(PlayerPlaying)
        case Status.EOM => Some(SongFinished)
        case _ => None
      }).foreach(observable.onNext)
    }
  })
  var volume: Percentage = 0.2
  private def trySetSource(s: Song): Task[Unit] = Task(try {
    s match {
      case e: LocalSong => sp.open(e.file)
      case e: RemoteSong => sp.open(new URL(c.path(e.remotePath)))
    }
    source = s
  } catch {
    case e: IllegalArgumentException =>
      println(s"Failed to set source for <$s>")
      throw e
  })

  override def setSource(s: Song): Task[Unit] =
    stop >> trySetSource(s)
  override def play: Task[Unit] = Task {
    assert(isPlaying.isFalse)
    if (isPaused) sp.resume() else sp.play()
    sp.setGain(volume.value)
  } unlessM isPlaying
  override def pause: Task[Unit] = Task(sp.pause()) unlessM isPaused
  override def setVolume(p: Percentage): Task[Unit] = Task {
    sp.setGain(p.value)
    volume = p
  }
  override def events: Observable[PlayerEvent] = observable.observeOn(IOPool.scheduler)
  private def isPlaying: Boolean = sp.isPlaying
  private def isPaused: Boolean = sp.isPaused
  private def isStopped: Boolean = sp.isStopped
  override def status: PlayerStatus = sp.getStatus match {
    case Status.PLAYING | Status.RESUMED | Status.SEEKED | Status.SEEKING | Status.BUFFERING => Playing
    case Status.STOPPED | Status.OPENED => Stopped
    case Status.PAUSED => Paused
    case Status.INIT => Initial
    case Status.EOM => EndOfSong
    case _ => ???
  }
  // TODO fix volume bullshit in StreamPlayer
  override def seek(p: Percentage) = Task(sp.seek(p * source.size)) >> setVolume(volume)
}
