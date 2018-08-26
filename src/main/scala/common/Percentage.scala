package common

import scala.language.implicitConversions

case class Percentage private(value: Double) extends AnyVal {
  def *(i: Int): Int = (value * i).toInt
  def *(l: Long): Long = (value * l).toLong
}

object Percentage {
  // One might wonder what's the point of an implicit def in this case, since it forgoes all type safety.
  // The answer is laziness (of course), and Percentage still enjoys the added benefit of bound checking at
  // runtime, as well as revealing intent to clients.
  implicit def toPercentage(d: Double): Percentage = Percentage(d)
  def apply(x: Double, y: Double): Percentage = apply(x / y)
  def apply(p: Double): Percentage = {
    require(p >= 0 && p <= 1)
    new Percentage(p)
  }
}
