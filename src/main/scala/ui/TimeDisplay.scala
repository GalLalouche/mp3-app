package ui

import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{PlayerEvent, TimeChange}
import rx.lang.scala.Observable

import scala.swing.Label

private class TimeDisplay @Inject()(events: Observable[PlayerEvent]) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  text = "00:00"

  events.observeOn(SwingEdtScheduler()).select[TimeChange].doOnNext(tc => text = tc.toDoubleDigitDisplay).subscribe()
}
