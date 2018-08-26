package ui.progress

import com.google.inject.Inject
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import player.{MutablePlayer, TimeChange}
import ui.{FillingBar, SwingEdtScheduler}

import scala.swing.{Dimension, Panel}

private class SeekBar @Inject()(player: MutablePlayer) extends Panel
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val filling = new FillingBar {
    preferredSize = new Dimension(800, 25)
  }
  _contents += filling
  filling.events.doOnNext(player.seek(_).fireAndForget()).subscribe()

  player.events
      .select[TimeChange]
      .map(_.percentage)
      .observeOn(SwingEdtScheduler())
      .doOnNext(filling.update)
      .subscribe()
}
