package ui.pp

import javax.inject.Inject
import ui.pp.playlist.PlaylistPanel
import ui.progress.{ProgressPanel, TimeDisplay}

import scala.swing.{BoxPanel, Dimension, Orientation, Panel}

private[ui] class PlayerPanel @Inject()(
    topButtons: TopButtons,
    posterComponent: PosterComponent,
    progressPanel: ProgressPanel,
    timeDisplay: TimeDisplay,
    controlStrip: ControlStrip,
    playlistPanel: PlaylistPanel
) extends Panel {
  preferredSize = new Dimension(800, 1000)
  _contents += new BoxPanel(Orientation.Vertical) {
    contents ++= Seq(
      topButtons,
      posterComponent,
      progressPanel,
      controlStrip,
      playlistPanel,
    )
  }
}
