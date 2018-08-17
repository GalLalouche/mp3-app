package ui

import comm.PosterComm
import common.IOPool
import common.RichTask._
import common.SwingUtils._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{MutablePlayer, SongChanged}

import scala.swing.{Dimension, Label}

class PosterComponent @Inject()(player: MutablePlayer, posterComm: PosterComm) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val SideLengthInPixels = 500
  preferredSize = new Dimension(SideLengthInPixels, SideLengthInPixels)

  player.events
      .observeOn(IOPool.scheduler)
      .select[SongChanged]
      .map(_.newSong)
      .flatMap(posterComm.poster(_).toObservable(IOPool()))
      .observeOn(SwingEdtScheduler())
      .doOnNext(e => icon = e.toSquareImageIcon(SideLengthInPixels))
      .subscribe()

  this.onMouseClick(player.togglePause.fireAndForget())
}
