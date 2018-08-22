package ui

import java.awt.{Component, Font}

import comm.{Instrumental, Lyrics, LyricsComm, WordLyrics}
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.swing.ImageIcon
import player.{CurrentChanged, PlayerEvent}
import rx.lang.scala.Observable

import scala.swing.{Dimension, Label, ScrollPane}

private class LyricsPanel @Inject()(
    events: Observable[PlayerEvent],
    lyricsComm: LyricsComm
) extends ScrollPane
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val trebleClef = new ImageIcon(ImageIO.read(getClass.getResourceAsStream("TrebleClef.png")))
  yLayoutAlignment = Component.BOTTOM_ALIGNMENT
  preferredSize = new Dimension(500, 1000)

  private val toLabel: Lyrics => Label = {
    case Instrumental => new Label {icon = trebleClef}
    case WordLyrics(lyrics) => new Label(lyrics) {
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
