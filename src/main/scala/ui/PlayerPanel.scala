package ui

import javax.inject.Inject
import ui.playlist.PlaylistPanel
import ui.progress.{ProgressPanel, TimeDisplay}

import scala.swing.{BoxPanel, Dimension, Orientation, Panel}

private class PlayerPanel @Inject()(
    posterComponent: PosterComponent,
    progressPanel: ProgressPanel,
    timeDisplay: TimeDisplay,
    controlStrip: ControlStrip,
    playlistPanel: PlaylistPanel
) extends Panel {
  preferredSize = new Dimension(800, 1000)
  _contents += new BoxPanel(Orientation.Vertical) {
    contents ++= Seq(
      posterComponent,
      progressPanel,
      controlStrip,
      playlistPanel,
    )
  }

  //playlistPanel.start().fireAndForget()
}
