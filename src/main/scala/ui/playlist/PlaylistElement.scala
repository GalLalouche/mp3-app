package ui.playlist

import java.awt.{Dimension, Font}

import javax.swing.BorderFactory
import javax.swing.border.BevelBorder
import player.Song
import rx.lang.scala.{Observable, Subject}

import scala.swing.Swing.HStrut
import scala.swing.{BorderPanel, BoxPanel, Button, Label, Orientation}

private class PlaylistElement(val s: Song) extends BorderPanel {
  import PlaylistElement._

  private val subject: Subject[PlaylistElementEvent] = Subject()
  def events: Observable[PlaylistElementEvent] = subject

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
        preferredSize = new Dimension(300, 35)
        peer.setBounds(0, 0, 300, 35)
        font = baseFont
      }
      contents += HStrut(5)
    },
    BorderPanel.Position.West)
  add(
    new BoxPanel(Orientation.Horizontal) {
      contents += Button("▶") {subject.onNext(PlayElement(PlaylistElement.this))}
      contents += HStrut(5)
      contents += Button("✖") {subject.onNext(RemoveElement(PlaylistElement.this))}
    }, BorderPanel.Position.East)

  revalidate()
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
