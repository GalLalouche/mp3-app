package ui.pp

import java.awt.Font

import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreApplicativeOps, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.{CurrentChanged, MutablePlayer, PlayerPaused, PlayerPlaying, PlayerStopped}
import ui.SwingEdtScheduler

import scala.swing.Swing.HStrut
import scala.swing.{Action, BoxPanel, Button, Orientation, Panel}

private class ControlStrip @Inject()(
    volumeControl: VolumeControl,
    player: MutablePlayer,
) extends Panel
    with ToMoreApplicativeOps with ToMoreMonadPlusOps with MoreObservableInstances {
  private val backwardsButton = Button("⏪") {player.previous unlessM player.isFirstSong fireAndForget()}
  player.events.observeOn(SwingEdtScheduler()).select[CurrentChanged]
      .map(_.index != 0)
      .doOnNext(backwardsButton.enabled_=)
      .subscribe()

  val playButton = Button("▶/❚❚") {???}
  player.events.observeOn(SwingEdtScheduler()).doOnNext {
    case PlayerStopped | PlayerPaused =>
      playButton.action = Action.apply("▶") {
        assert(player.isPaused || player.isStopped)
        player.playCurrentSong.fireAndForget()
      }
    case PlayerPlaying =>
      playButton.action = Action.apply("❚❚") {
        assert(player.isPlaying)
        player.pause.fireAndForget()
      }
    case _ => ()
  }.subscribe()
  _contents += new BoxPanel(Orientation.Horizontal) {
    font = new Font("Helvetica", Font.BOLD, 16)
    contents ++= Seq(
      backwardsButton,
      playButton,
      Button("■") {player.stop.fireAndForget()},
      Button("⏩") {
        player.next.fireAndForget()
      },
      HStrut(5),
      volumeControl,
    )
  }
}
