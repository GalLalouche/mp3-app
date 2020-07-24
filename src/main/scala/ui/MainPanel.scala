package ui

import java.awt.event.KeyEvent
import java.awt.{Dimension, KeyboardFocusManager}

import common.RichTask._
import javax.inject.Inject
import player.MutablePlayer
import ui.external.ExternalLinksPanel
import ui.pp.PlayerPanel

import scala.swing.{BoxPanel, Orientation, Panel}

private class MainPanel @Inject()(
    player: MutablePlayer,
    playerPanel: PlayerPanel,
    externalLinksPanel: ExternalLinksPanel,
    lyricsPanel: LyricsPanel,
) extends Panel {
  listenTo(keys)
  KeyboardFocusManager.getCurrentKeyboardFocusManager.addKeyEventDispatcher(event => {
    if (event.getID == KeyEvent.KEY_RELEASED)
      event.getKeyChar match {
        case 'k' => player.togglePause.fireAndForget()
        case 'b' => player.next.fireAndForget()
        case 'z' => player.previous.fireAndForget()
        case _ => ()
      }
    false
  })
  preferredSize = new Dimension(1500, 1000)
  _contents += new BoxPanel(Orientation.Horizontal) {
    contents ++= Seq(
      playerPanel,
      externalLinksPanel,
      lyricsPanel
    )
  }
}
