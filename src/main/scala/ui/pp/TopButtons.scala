package ui.pp

import java.io.File

import common.RichTask._
import common.rich.path.RichFile._
import javax.inject.Inject
import javax.swing.filechooser.FileFilter
import player.MutablePlayer
import player.pkg.AlbumPackager

import scala.swing.FileChooser.SelectionMode
import scala.swing.{Button, FileChooser, Panel}

private class TopButtons @Inject()(
    albumPackager: AlbumPackager,
    player: MutablePlayer,
) extends Panel {
  private val fileChooser = new FileChooser(new File("/Users/glalouche/Downloads")) {
    fileFilter = new FileFilter {
      override def accept(f: File): Boolean = f.extension == "zip" || f.isDirectory
      override def getDescription: String = "Packaged album"
    }

    fileHidingEnabled = true
    multiSelectionEnabled = false
    peer.setAcceptAllFileFilterUsed(false)
    fileSelectionMode = SelectionMode.FilesOnly
  }
  _contents += Button("add zip file") {
    fileChooser.showOpenDialog(this)
    val file = fileChooser.selectedFile
    if (file != null)
      player.add(albumPackager.packageFromZip(file)).fireAndForget()
  }
}
