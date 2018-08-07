package ui

import java.awt.Dimension

import javax.inject.Inject

import scala.swing.{BoxPanel, Orientation, Panel}

private class MainPanel @Inject()(playerPanel: PlayerPanel) extends Panel {
  preferredSize = new Dimension(1500, 1000)
  _contents += new BoxPanel(Orientation.Horizontal) {
    contents += playerPanel
  }
}
