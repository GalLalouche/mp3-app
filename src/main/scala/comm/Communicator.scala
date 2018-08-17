package comm

import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import scalaz.concurrent.Task
import spray.json.{JsonReader, enrichString}

trait Communicator {
  def path(remotePath: String): String
  def getBytes(path: String): Task[Array[Byte]]
  def getString(path: String): Task[String] = getBytes(path).map(new String(_, "UTF-8"))
  def parseJson[A: JsonReader](path: String): Task[A] = getString(path).map(_.parseJson.convertTo[A])
}

object Communicator {
  class From @Inject()(talker: InternetTalker, @ServerAddress host: String) extends Communicator {
    private val fixedHost = host.mapIf(_.endsWith("/").isFalse).to(_ + "/")
    override def path(p: String): String = fixedHost + p.mapIf(_.startsWith("/")).to(p.tail)
    override def getBytes(p: String): Task[Array[Byte]] = talker.get(path(p))
  }
}
