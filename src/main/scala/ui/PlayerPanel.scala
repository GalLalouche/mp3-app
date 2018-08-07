package ui

import common.RichTask._
import javax.inject.Inject

import scala.swing.{BoxPanel, Dimension, Orientation, Panel}

private class PlayerPanel @Inject()(
    posterComponent: PosterComponent,
    timeDisplay: TimeDisplay,
    controlStrip: ControlStrip,
    playlistPanel: PlaylistPanel
) extends Panel {
  preferredSize = new Dimension(800, 1000)
  _contents += new BoxPanel(Orientation.Vertical) {
    contents ++= Seq(
      posterComponent,
      timeDisplay,
      controlStrip,
      playlistPanel,
    )
  }

  playlistPanel.start().fireAndForget()
}
