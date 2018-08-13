package common

case class Percentage private(p: Double) extends AnyVal {
  def *(i: Int): Int = (p * i).toInt
  def *(l: Long): Long = (p * l).toLong
}

object Percentage {
  def apply(x: Double, y: Double): Percentage = apply(x / y)
  def apply(p: Double): Percentage = {
    require(p >= 0 && p <= 1)
    new Percentage(p)
  }
}
