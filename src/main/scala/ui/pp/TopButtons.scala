package ui.pp

import java.io.File

import javax.swing.filechooser.FileFilter

import scala.swing.{Button, FileChooser, Panel}
import common.rich.path.RichFile._

private class TopButtons extends Panel {
  private val fileChooser = new FileChooser() {
    fileFilter =  new FileFilter {
      override def accept(f: File): Boolean = f.extension == "zip" || f.isDirectory
      override def getDescription: String = "Packaged album"
    }
  }
  _contents += Button("add zip file") {
    fileChooser.showOpenDialog(this)
  }
}
