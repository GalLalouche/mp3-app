package ui

import comm.RandomSong
import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import player.playlist.{CurrentChanged, Playlist, SongAdded}
import scalaz.concurrent.Task

import scala.swing.{BoxPanel, Orientation, Panel}

private class PlaylistPanel @Inject()(playlist: Playlist, randomSong: RandomSong) extends Panel
    with ToMoreFunctorOps {
  private val box = new BoxPanel(Orientation.Vertical)
  _contents += box

  private def elements: Seq[PlaylistElement] = box.contents.toVector.map(_.asInstanceOf[PlaylistElement]).reverse
  playlist.events.observeOn(SwingEdtScheduler()).doOnNext({
    case SongAdded(s, _) => box.contents prepend new PlaylistElement(s)
    case CurrentChanged(_, index) =>
      elements.foreach(_.unHighlight())
      elements(index).highlight()
    case _ => ()
  }).subscribe()

  def start(): Task[Unit] = {
    assert(playlist.isEmpty)
    for {
      _ <- playlist.player.setVolume(0.0)
      song <- randomSong.randomSong
      _ <- playlist.add(song)
      _ <- playlist.playCurrentSong
    } yield ()
  }
}
