package ui

import java.awt.RenderingHints
import java.awt.image.BufferedImage

import comm.PosterComm
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import javax.swing.ImageIcon
import player.{Player, SongChanged}

import scala.swing.{Dimension, Image, Label}

class PosterComponent @Inject()(player: Player, posterComm: PosterComm) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val playerEvents = player.events
  private val SideLengthInPixels = 500
  preferredSize = new Dimension(SideLengthInPixels, SideLengthInPixels)

  private def setImage(image: Image): Unit = {
    val bi = new BufferedImage(SideLengthInPixels, SideLengthInPixels, BufferedImage.TYPE_INT_ARGB)
    val g2 = bi.createGraphics

    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2.drawImage(image, 0, 0, SideLengthInPixels, SideLengthInPixels, null)
    g2.dispose()

    icon = new ImageIcon(bi)
  }

  playerEvents
      .observeOn(IOPool.scheduler)
      .select[SongChanged]
      .map(_.newSong)
      .flatMap(posterComm.poster(_).toObservable(IOPool()))
      .observeOn(SwingEdtScheduler())
      .doOnNext(setImage)
      .subscribe()
}
