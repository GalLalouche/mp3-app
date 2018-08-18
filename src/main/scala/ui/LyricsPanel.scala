package ui

import comm.{Instrumental, Lyrics, LyricsComm, WordLyrics}
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.swing.ImageIcon
import player.{CurrentChanged, PlayerEvent}
import rx.lang.scala.Observable

import scala.swing.{Dimension, Label, Panel}

class LyricsPanel @Inject()(
    events: Observable[PlayerEvent],
    lyricsComm: LyricsComm
) extends Panel
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val trebleClef = new ImageIcon(ImageIO.read(getClass.getResourceAsStream("TrebleClef.png")))
  preferredSize = new Dimension(500, 300)

  private val toLabel: Lyrics => Label = {
    case Instrumental => new Label {icon = trebleClef}
    case WordLyrics(lyrics) => new Label(lyrics)
  }

  events.select[CurrentChanged]
      .map(_.s)
      .flatMap(lyricsComm(_).toObservable)
      .map(toLabel)
      .observeOn(SwingEdtScheduler())
      .doOnNext(l => {
        _contents.clear()
        _contents += l
        repaint()
      }).subscribe()
}
