package ui.progress

import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{PlayerEvent, TimeChange}
import rx.lang.scala.Observable
import ui.SwingEdtScheduler

import scala.swing.Label

class TimeDisplay @Inject()(events: Observable[PlayerEvent]) extends Label
    with ToMoreMonadPlusOps with MoreObservableInstances {
  text = "00:00"

  events
      .select[TimeChange]
      .map(_.toDoubleDigitDisplay)
      .observeOn(SwingEdtScheduler())
      .doOnNext(text = _)
      .subscribe()
}
