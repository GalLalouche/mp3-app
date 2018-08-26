package ui

import java.awt.{Color, Polygon}

import common.SwingUtils._
import common.{IOPool, Percentage}
import rx.lang.scala.{Observable, Subject}

import scala.swing.{Component, Graphics2D}

private[ui] class FillingBar extends Component {
  private var percentage = Percentage(0)
  def update(percentage: Percentage): Unit = synchronized {
    this.percentage = percentage
    repaint()
  }

  override def paintComponent(g2D: Graphics2D): Unit = {
    super.paintComponent(g2D)

    // Background
    g2D.setColor(Color.LIGHT_GRAY)
    val width = preferredSize.width
    val height = preferredSize.height
    g2D.fillRect(0, 5, width, height - 10)

    // Percentage done
    g2D.setColor(Color.DARK_GRAY)
    val fillLocation = percentage * width
    g2D.fillRect(0, 5, fillLocation, height - 10)

    //// Draw arrow that scales with thickness of SeekBar
    g2D.setColor(Color.BLUE)
    val arrowPoly = new Polygon(
      Array(-5, -5, 0, 5, 5),
      Array(0, 3 * height / 4, height, 3 * height / 4, 0),
      5)
    arrowPoly.translate(fillLocation, 0)
    g2D.fillPolygon(arrowPoly)
  }

  private val subject = Subject[Percentage]()
  this.onMouseClick(me => subject.onNext(Percentage(me.getX, this.preferredSize.width)))

  /** Percentage of X clicked on. */
  val events: Observable[Percentage] = subject.observeOn(IOPool.scheduler)
}
