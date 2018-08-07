package comm

import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import scalaz.concurrent.Task

trait Communicator {
  def path(remotePath: String): String
  def getBytes(path: String): Task[Array[Byte]]
  def getString(path: String): Task[String] = getBytes(path).map(new String(_, "UTF-8"))
}

object Communicator {
  class From @Inject()(talker: InternetTalker, @ServerAddress host: String) extends Communicator {
    private val fixedHost = host.mapIf(_.endsWith("/").isFalse).to(_ + "/")
    override def path(p: String): String = fixedHost + p.mapIf(_.startsWith("/")).to(p.tail)
    override def getBytes(p: String): Task[Array[Byte]] = talker.get(path(p))
  }
}
