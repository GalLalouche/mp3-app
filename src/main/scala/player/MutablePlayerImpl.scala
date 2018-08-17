package player

import common.IOPool
import rx.lang.scala.{Observable, Subject}
import scalaz.concurrent.Task
import scalaz.syntax.{ToBindOps, ToMonadOps}

private class MutablePlayerImpl(
    audioPlayer: AudioPlayer,
    var playlist: UpdatablePlaylist,
    songFetcher: SongFetcher,
) extends MutablePlayer
    with ToMonadOps with ToBindOps {
  private def emitStatus() = subject.onNext(StatusChanged(status))
  private def emitCurrentChanged() = subject.onNext(CurrentChanged(currentSong, currentIndex))
  private def updatePlaylist(f: UpdatablePlaylist => UpdatablePlaylist): Task[Unit] =
    Task(playlist = f(playlist)).void

  private val subject = Subject[PlayerEvent]()
  override val events: Observable[PlayerEvent] = subject.observeOn(IOPool.scheduler)

  override def setIndex(index: Int): Task[Unit] =
    if (index < 0) throw new IndexOutOfBoundsException(s"Invalid index <$index>")
    else stop >> updatePlaylist(_ setIndex index) >| emitCurrentChanged()
  override def playCurrentSong: Task[Unit] = {
    assert(audioPlayer.source == currentSong)
    (audioPlayer.play >| emitStatus()).unlessM(status == Playing)
  }
  override def add(s: Song): Task[Unit] =
    audioPlayer.setSource(s).whenM(isEmpty) >> updatePlaylist(_ add s) >| subject.onNext(SongAdded(s, currentIndex))
  override def stop: Task[Unit] = audioPlayer.stop >| emitStatus
  override def next: Task[Unit] = {
    val wasPlaying = status == Playing
    if (playlist.isLastSong)
      songFetcher.apply.>>=(add).>>(next)
    else
      stop >> updatePlaylist(_.next) >> audioPlayer.setSource(currentSong) >> playCurrentSong.whenM(wasPlaying)
  } >| emitCurrentChanged()

  override def previous: Task[Unit] = updatePlaylist(_.previous) >| emitCurrentChanged()
  override def status: PlayerStatus = audioPlayer.status
}


