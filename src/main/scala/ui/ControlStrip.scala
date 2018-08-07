package ui

import comm.RandomSong
import common.RichTask._
import javax.inject.Inject
import player.playlist.Playlist
import scalaz.concurrent.Task
import scalaz.syntax.ToBindOps

import scala.swing.{BoxPanel, Button, Orientation, Panel}

private class ControlStrip @Inject()(playlist: Playlist, randomSong: RandomSong) extends Panel
    with ToBindOps {
  private val box = new BoxPanel(Orientation.Horizontal)

  box.contents += Button("▶/❚❚") {playlist.playOrPause.fireAndForget()}
  box.contents += Button("■") {playlist.stop.fireAndForget()}
  box.contents += Button("⏩") {
    val t = if (playlist.lastSong) randomSong.randomSong.flatMap(playlist.add) else Task.delay(Unit)
    t.>>(playlist.next).fireAndForget()
  }

  _contents += box
}
