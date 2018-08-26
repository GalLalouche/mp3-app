package ui.pp

import common.RichTask._
import javax.inject.Inject
import player.MutablePlayer
import ui.FillingBar

import scala.swing.{Dimension, Panel}

class VolumeControl @Inject()(player: MutablePlayer) extends Panel {
  private val fillingBar = new FillingBar {
    preferredSize = new Dimension(100, 20)
  }
  fillingBar.update(player.volume.value)

  fillingBar.events.doOnNext(fillingBar.update).flatMap(player.setVolume(_).toObservable).subscribe()

  _contents += fillingBar
}
