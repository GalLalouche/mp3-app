package ui

import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{Player, TimeChange}

import scala.swing.Label

private class TimeDisplay @Inject()(player: Player) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  text = "00:00"

  player.events.observeOn(SwingEdtScheduler()).select[TimeChange].doOnNext(tc => text = tc.toDoubleDigitDisplay).subscribe()
}
