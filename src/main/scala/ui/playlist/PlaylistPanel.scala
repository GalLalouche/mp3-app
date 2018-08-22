package ui.playlist

import java.awt.Color

import common.RichTask._
import common.rich.collections.RichSeq._
import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import javax.swing.BorderFactory
import player.{CurrentChanged, MutablePlayer, Song, SongAdded, SongFetcher, SongRemoved}
import scalaz.concurrent.Task
import scalaz.syntax.ToBindOps
import ui.SwingEdtScheduler

import scala.swing.ScrollPane.BarPolicy
import scala.swing.{BoxPanel, Dimension, Orientation, Panel, ScrollPane}

private[ui] class PlaylistPanel @Inject()(playlist: MutablePlayer, songFetcher: SongFetcher) extends Panel
    with ToMoreFunctorOps with ToBindOps {
  private val box = new BoxPanel(Orientation.Vertical)
  _contents += new ScrollPane {
    contents = box
    preferredSize = new Dimension(700, 400)
    verticalScrollBarPolicy = BarPolicy.Always
    horizontalScrollBarPolicy = BarPolicy.AsNeeded
  }
  ignoreRepaint = false
  border = BorderFactory.createLineBorder(Color.BLACK)

  private def elements: Seq[PlaylistElement] = box.contents.toVector.map(_.asInstanceOf[PlaylistElement]).reverse
  private def indexOf(e: PlaylistElement): Int = elements.findIndex(_.s == e.s).get
  private def elementWith(s: Song): PlaylistElement = elements.find(_.s == s).get
  private def removeWith(s: Song): Unit = {
    box.peer.remove(elementWith(s).peer)
  }

  playlist.events.observeOn(SwingEdtScheduler()).doOnNext({
    case SongAdded(s, _) =>
      val element = new PlaylistElement(s)
      element.events.doOnNext({
        case PlayElement(source) =>
          (playlist.setIndex(indexOf(source)) >> playlist.stop >> playlist.playCurrentSong).fireAndForget()
        case RemoveElement(source) => ???
      }).subscribe()
      element +=: box.contents
      box.revalidate()
    case CurrentChanged(song, _) =>
      elements.foreach(_.unHighlight())
      elementWith(song).highlight()
    case SongRemoved(s, _) =>
      removeWith(s)
      box.revalidate()
    case _ => ()
  }).subscribe()

  def start(): Task[Unit] = {
    assert(playlist.isEmpty)
    for {
      _ <- playlist.setVolume(0.0)
      song <- songFetcher.apply
      _ <- playlist.add(song)
      _ <- playlist.playCurrentSong
    } yield ()
  }
}
