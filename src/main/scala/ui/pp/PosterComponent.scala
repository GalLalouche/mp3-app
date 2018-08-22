package ui.pp

import comm.PosterComm
import common.IOPool
import common.RichTask._
import common.SwingUtils._
import common.rich.RichT._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{CurrentChanged, MutablePlayer}
import ui.SwingEdtScheduler

import scala.swing.{Dimension, Label}

private class PosterComponent @Inject()(player: MutablePlayer, posterComm: PosterComm) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val SideLengthInPixels = 500
  preferredSize = new Dimension(SideLengthInPixels, SideLengthInPixels)
  minimumSize = new Dimension(SideLengthInPixels, SideLengthInPixels)
  maximumSize = new Dimension(SideLengthInPixels, SideLengthInPixels)

  player.events
      .observeOn(IOPool.scheduler)
      .select[CurrentChanged]
      .map(_.s)
      .flatMap(posterComm.poster(_).toObservable)
      .observeOn(SwingEdtScheduler())
      .doOnNext(e => icon = e.toSquareImageIcon(SideLengthInPixels))
      .subscribe()

  this.onMouseClick(player.togglePause.fireAndForget().const)
}
