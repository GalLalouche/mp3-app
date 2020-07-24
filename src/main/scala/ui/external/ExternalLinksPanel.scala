package ui.external

import comm.external.ExternalLinksComm
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{CurrentChanged, PlayerEvent}
import rx.lang.scala.Observable
import ui.SwingEdtScheduler

import scala.swing.{BoxPanel, Dimension, Orientation, Panel}

private[ui] class ExternalLinksPanel @Inject()(
    events: Observable[PlayerEvent],
    externalLinksComm: ExternalLinksComm
) extends Panel
    with ToMoreMonadPlusOps with MoreObservableInstances {
  private val artistLinks = new ExternalLinksAux
  private val albumLinks = new ExternalLinksAux
  _contents += new BoxPanel(Orientation.Vertical) {
    preferredSize = new Dimension(350, 500)
    contents ++= Seq(
      artistLinks,
      albumLinks,
    )
  }

  events.select[CurrentChanged]
      .map(_.s)
      .flatMap(externalLinksComm.links(_).toObservable)
      .observeOn(SwingEdtScheduler())
      .doOnNext(el => {
        artistLinks.update(el.artistLinks)
        albumLinks.update(el.albumLinks)
        validate()
        revalidate()
        repaint()
        validate()
        revalidate()
        repaint()
      }).subscribe()
}
