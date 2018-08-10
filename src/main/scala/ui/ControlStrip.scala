package ui

import comm.RandomSong
import common.RichTask._
import common.rich.func.{MoreObservableInstances, ToMoreApplicativeOps, ToMoreMonadPlusOps}
import javax.inject.Inject
import player.playlist.{CurrentChanged, Playlist}

import scala.swing.{BoxPanel, Button, Orientation, Panel}

private class ControlStrip @Inject()(playlist: Playlist, randomSong: RandomSong) extends Panel
    with ToMoreApplicativeOps with ToMoreMonadPlusOps with MoreObservableInstances {
  private val backwardsButton = Button("⏪") {playlist.previous unlessM playlist.isFirstSong fireAndForget()}
  playlist.events.observeOn(SwingEdtScheduler()).select[CurrentChanged]
      .map(_.index != 0)
      .doOnNext(backwardsButton.enabled_=)
      .subscribe()

  _contents += new BoxPanel(Orientation.Horizontal) {
    contents ++= Seq(
      backwardsButton,
      Button("▶/❚❚") {playlist.playOrPause.fireAndForget()},
      Button("■") {playlist.stop.fireAndForget()},
      Button("⏩") {
        randomSong.randomSong.flatMap(playlist.add).whenM(playlist.isLastSong) >> playlist.next fireAndForget()
      },
    )
  }
}
