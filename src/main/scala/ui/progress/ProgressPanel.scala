package ui.progress

import java.awt.Dimension

import javax.inject.Inject

import scala.swing.BorderPanel

class ProgressPanel @Inject()(
    timeDisplay: TimeDisplay,
    seekBar: SeekBar
) extends BorderPanel {
  preferredSize = new Dimension(800, 50)
  add(timeDisplay, BorderPanel.Position.Center)
  add(seekBar, BorderPanel.Position.South)
}
