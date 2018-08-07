package ui

import java.awt.{Dimension, Font}

import javax.swing.BorderFactory
import javax.swing.border.BevelBorder
import player.Song
import ui.PlaylistElement._

import scala.swing.Swing.HStrut
import scala.swing.{BorderPanel, BoxPanel, Button, Label, Orientation}

private class PlaylistElement(s: Song) extends BorderPanel {
  private val fullText = s"<html><b>${s.title}</b> by ${s.artistName} (${s.albumName}, ${s.track}, ${s.formattedLength}, ${s.bitrate}kbps)</html>"
  border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
  preferredSize = new Dimension(600, 35)
  add(
    new BoxPanel(Orientation.Horizontal) {
      contents += HStrut(5)
      contents += new Label(s.title) {
        font = baseFont.deriveFont(Font.BOLD)
      }
      contents += new Label(s" by ${s.artistName} (${s.albumName}, ${s.track}, ${s.formattedLength}, ${s.bitrate}kbps)") {
        preferredSize = new Dimension(350, 35)
        font = baseFont
      }
      contents += HStrut(5)
    },
    BorderPanel.Position.West)
  add(
    new BoxPanel(Orientation.Horizontal) {
      contents += Button("▶") {???}
      contents += HStrut(5)
      contents += Button("✖") {???}
    }, BorderPanel.Position.East)

  def highlight(): Unit = {
    border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
  }
  def unHighlight(): Unit = {
    border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
  }
  tooltip = fullText
}

private object PlaylistElement {
  private val baseFont = new Font("SansSerif", Font.PLAIN, 12)
}
