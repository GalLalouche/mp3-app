package ui.external

import comm.external.ExternalLinksComm
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{CurrentChanged, PlayerEvent}
import rx.lang.scala.Observable
import ui.SwingEdtScheduler

import scala.swing.{BoxPanel, Dimension, Orientation}

private[ui] class ExternalLinksPanel @Inject()(
    events: Observable[PlayerEvent],
    externalLinksComm: ExternalLinksComm
) extends BoxPanel(Orientation.Vertical)
    with ToMoreMonadPlusOps with MoreObservableInstances {
  preferredSize = new Dimension(500, 1000)
  private val artistLinks = new ExternalLinksAux
  private val albumLinks = new ExternalLinksAux
  contents += artistLinks
  contents += albumLinks

  events.select[CurrentChanged]
      .map(_.s)
      .flatMap(externalLinksComm.links(_).toObservable)
      .observeOn(SwingEdtScheduler())
      .doOnNext(el => {
        artistLinks.update(el.artistLinks)
        albumLinks.update(el.albumLinks)
        validate()
      }).subscribe()
}
