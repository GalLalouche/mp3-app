package common

import org.scalatest.Tag

// TODO Move to common
object Regression {
  def regression: Tag = regression("Regression")
  def regression(reason: String): Tag = Tag("Regression: " + reason)
}
