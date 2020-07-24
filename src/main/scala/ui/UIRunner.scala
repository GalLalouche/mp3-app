package ui

import java.io.File

import com.google.inject.{Guice, Injector}
import net.codingwell.scalaguice.InjectorExtensions._
import player.MutablePlayer
import player.pkg.AlbumPackager

import scala.swing.MainFrame

object UIRunner {
  private def debug(injector: Injector) = {
    val player = injector.instance[MutablePlayer]
    val packager = injector.instance[AlbumPackager]
    player.add(packager.packageFromZip(new File("/Users/glalouche/Downloads/1995.zip"))).unsafePerformSync
  }

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(AllModules)
    val frame = new MainFrame
    frame.contents = injector.instance[MainPanel]
    frame.open()

    //debug(injector)
  }
}
