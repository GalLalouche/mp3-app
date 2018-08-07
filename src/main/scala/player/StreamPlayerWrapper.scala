package player

import java.net.URL
import java.util
import java.util.logging.{Level, Logger}

import comm.Communicator
import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import main.java.goxr3plus.javastreamplayer.stream.{StreamPlayer, StreamPlayerEvent, StreamPlayerListener}
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task

class StreamPlayerWrapper private[player](c: Communicator, sp: StreamPlayer) extends Player
    with ToMoreFunctorOps {
  @Inject() def this(c: Communicator) = this(c, new StreamPlayer)
  Logger.getLogger("main.java.goxr3plus").setLevel(Level.WARNING)
  def stop: Task[Unit] = Task(sp.stop())

  private val observable = Subject[PlayerEvent]()

  sp.addStreamPlayerListener(new StreamPlayerListener {
    override def opened(dataSource: scala.Any, properties: util.Map[String, AnyRef]): Unit = ()
    override def progress(nEncodedBytes: Int, microsecondPosition: Long, pcmData: Array[Byte], properties: util.Map[String, AnyRef]): Unit =
      observable.onNext(TimeChange.fromMicrosecond(microsecondPosition))
    override def statusUpdated(event: StreamPlayerEvent): Unit = ()
  })
  private var volume: Double = _
  override def setSource(s: Song): Task[Unit] =
    stop >| sp.open(new URL(c.path(s.path))) >| observable.onNext(SongChanged(s))
  override def play: Task[Unit] = Task {
    if (sp.isPausedOrPlaying)
      sp.stop()
    sp.play()
    sp.setGain(volume)
  }
  override def pause: Task[Unit] = Task(sp.pause())
  override def isPaused: Boolean = sp.isPausedOrPlaying
  override def setVolume(f: Double): Task[Unit] = Task {
    require(f >= 0 && f <= 1)
    sp.setGain(f)
    volume = f
  }
  override def events: Observable[PlayerEvent] = observable
  override def isPlaying: Boolean = sp.isPlaying
}
