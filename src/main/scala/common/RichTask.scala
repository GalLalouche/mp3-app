package common

import rx.lang.scala.Observable
import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext

object RichTask {
  implicit class richTask[A]($: Task[A]) {
    def toObservable(ec: ExecutionContext): Observable[A] = Observable.apply(s => {
      ec.execute(() => {
        s.onNext($.unsafePerformSync)
      })
    })
  }

  implicit class richUnitTask($: Task[Unit]) {
    def fireAndForget(): Unit = $.unsafePerformAsync({
      case -\/(e) =>
        e.printStackTrace()
        // TODO log errors
        throw e
      case \/-(_) => ()
    })
  }
}
