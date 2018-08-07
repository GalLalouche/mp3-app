package comm

import scalaz.concurrent.Task

trait InternetTalker {
  def get(url: String): Task[Array[Byte]]
}
