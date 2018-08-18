package player

import common.IOPool
import common.RichTask._
import common.rich.func.{MoreObservableInstances, MoreTraverseInstances, ToMoreApplicativeOps}
import javax.inject.Inject
import player.pkg.PackagedAlbum
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.{ToMonadOps, ToTraverseOps}

private class MutablePlayerImpl @Inject()(
    audioPlayer: AudioPlayer,
    var playlist: UpdatablePlaylist,
    songFetcher: SongFetcher,
) extends MutablePlayer
    with ToMonadOps with ToTraverseOps with ToMoreApplicativeOps
    with MoreTraverseInstances with MoreObservableInstances{
  private def emitStatus() = subject.onNext(StatusChanged(status))
  private def emitCurrentChanged() = subject.onNext(CurrentChanged(currentSong, currentIndex))
  private def updatePlaylist(f: UpdatablePlaylist => UpdatablePlaylist): Task[Unit] =
    Task(playlist = f(playlist)).void

  private val subject = Subject[PlayerEvent]()
  override val events: Observable[PlayerEvent] = subject.observeOn(IOPool.scheduler)

  audioPlayer.events.doOnNext(subject.onNext).subscribe()
  audioPlayer.events.filter(_ == SongFinished).doOnNext(_ => next.fireAndForget()).subscribe()

  override def setIndex(index: Int): Task[Unit] =
    if (index < 0) throw new IndexOutOfBoundsException(s"Invalid index <$index>")
    else stop >> updatePlaylist(_ setIndex index) >| emitCurrentChanged()
  override def playCurrentSong: Task[Unit] = {
    assert(audioPlayer.source == currentSong)
    (audioPlayer.play >| emitStatus()).unlessM(status == Playing)
  }
  override def add(s: Song): Task[Unit] = for {
    empty <- Task.delay(isEmpty)
    _ <- updatePlaylist(_ add s) >| subject.onNext(SongAdded(s, currentIndex))
    // TODO add this in ToMoreApplicativeOps
    _ <- audioPlayer.setSource(s) >| subject.onNext(CurrentChanged(s, 0)) if empty
  } yield ()

  override def add(pkg: PackagedAlbum): Task[Unit] = pkg.songs.toList.map(add).sequenceU.void
  override def stop: Task[Unit] = audioPlayer.stop >| emitStatus
  override def pause: Task[Unit] = audioPlayer.pause >| emitStatus
  override def next: Task[Unit] = {
    val wasPlaying = status == Playing
    if (playlist.isLastSong)
      songFetcher.apply.>>=(add).>>(next)
    else
      stop >> updatePlaylist(_.next) >> audioPlayer.setSource(currentSong) >> playCurrentSong.whenM(wasPlaying)
  } >| emitCurrentChanged()

  override def previous: Task[Unit] = updatePlaylist(_.previous) >| emitCurrentChanged()
  override def status: PlayerStatus = audioPlayer.status

  override def setVolume(d: Double) = audioPlayer.setVolume(d)
}
