package ui

import comm.RandomSong
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreApplicativeOps, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.playlist.Playlist
import player.{AudioPlayer, CurrentChanged, PlayerPaused, PlayerPlaying, PlayerStopped}

import scala.swing.{Action, BoxPanel, Button, Orientation, Panel}

private class ControlStrip @Inject()(playlist: Playlist, randomSong: RandomSong) extends Panel
    with ToMoreApplicativeOps with ToMoreMonadPlusOps with MoreObservableInstances {
  private val player: AudioPlayer = playlist.player

  private val backwardsButton = Button("⏪") {playlist.previous unlessM playlist.isFirstSong fireAndForget()}
  playlist.events.observeOn(SwingEdtScheduler()).select[CurrentChanged]
      .map(_.index != 0)
      .doOnNext(backwardsButton.enabled_=)
      .subscribe()

  val playButton = Button("▶/❚❚") {???}
  player.events.observeOn(SwingEdtScheduler()).doOnNext {
    case PlayerStopped | PlayerPaused =>
      playButton.action = Action.apply("▶") {
        assert(player.isPaused || player.isStopped)
        player.play.fireAndForget()
      }
    case PlayerPlaying =>
      playButton.action = Action.apply("❚❚") {
        assert(player.isPlaying)
        player.pause.fireAndForget()
      }
    case _ => ()
  }.subscribe()
  _contents += new BoxPanel(Orientation.Horizontal) {
    contents ++= Seq(
      backwardsButton,
      playButton,
      Button("■") {playlist.stop.fireAndForget()},
      Button("⏩") {
        randomSong.randomSong.flatMap(playlist.add).whenM(playlist.isLastSong) >> playlist.next fireAndForget()
      },
    )
  }
}
