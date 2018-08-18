package common

import java.io.File
import java.net.URLDecoder

// TODO move to ScalaCommon
object RichClass {
  implicit class richClass($: Class[_]) {
    def getResourceFile(name: String): File = new File(URLDecoder.decode($.getResource(name).getPath, "utf-8"))
  }
}
