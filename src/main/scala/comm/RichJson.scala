package comm

import spray.json.{JsString, JsValue}

object RichJson {
  implicit class richJson($: JsValue) {
    private val asObject = $.asJsObject
    def /(path: String): JsValue = asObject.fields(path)
    def str(path: String): String = asObject.fields(path).asInstanceOf[JsString].value
  }
}
