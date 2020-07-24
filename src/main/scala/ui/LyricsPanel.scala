package ui

import java.awt.{Component, Font}

import comm.{Instrumental, Lyrics, LyricsComm, WordLyrics}
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.swing.{BorderFactory, ImageIcon}
import player.{CurrentChanged, PlayerEvent}
import rx.lang.scala.Observable

import scala.swing.ScrollPane.BarPolicy
import scala.swing.{Alignment, Dimension, Label, ScrollPane}

private class LyricsPanel @Inject()(
    events: Observable[PlayerEvent],
    lyricsComm: LyricsComm
) extends ScrollPane
    with ToMoreMonadPlusOps with MoreObservableInstances {
  verticalScrollBarPolicy = BarPolicy.AsNeeded
  private val trebleClef = new ImageIcon(ImageIO.read(getClass.getResourceAsStream("TrebleClef.png")))
  yLayoutAlignment = Component.BOTTOM_ALIGNMENT
  xLayoutAlignment = Component.LEFT_ALIGNMENT
  preferredSize = new Dimension(500, 1900)

  private val toLabel: Lyrics => Label = {
    case Instrumental => new Label {
      verticalAlignment = Alignment.Top
      icon = trebleClef
    }
    case WordLyrics(lyrics) => new Label(lyrics) {
      preferredSize = new Dimension(450, 1900)
      border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
      horizontalAlignment = Alignment.Left
      verticalAlignment = Alignment.Top
      font = new Font("Helvetica", Font.PLAIN, 16)
    }
  }

  events.select[CurrentChanged]
      .map(_.s)
      .flatMap(lyricsComm(_).toObservable)
      .map(toLabel)
      .observeOn(SwingEdtScheduler())
      .doOnNext(l => {
        contents = l
        repaint()
      }).subscribe()
}
