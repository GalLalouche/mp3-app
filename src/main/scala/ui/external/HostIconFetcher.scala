package ui.external

import javax.imageio.ImageIO
import javax.swing.ImageIcon

private object HostIconFetcher {
  // TODO generalize with LyricsPanel
  def apply(host: String): ImageIcon =
    new ImageIcon(ImageIO.read(getClass.getResourceAsStream(host.toLowerCase + "_icon.png")))
}
