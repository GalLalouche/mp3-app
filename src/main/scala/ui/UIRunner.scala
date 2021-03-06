package ui

import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._

import scala.swing.MainFrame

object UIRunner {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(AllModules)
    val frame = new MainFrame
    frame.contents = injector.instance[MainPanel]
    frame.open()
  }
}
