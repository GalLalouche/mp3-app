package comm

import scalaj.http.Http
import scalaz.concurrent.Task

private object ScalaJTalker extends InternetTalker {
  override def get(url: String): Task[Array[Byte]] = Task(Http(url).asBytes.body)
}
