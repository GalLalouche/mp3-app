package common

import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ExecutionContextScheduler
import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

import scala.concurrent.ExecutionContext

object RichTask {
  implicit class richTask[A]($: Task[A]) {
    def toObservable: Observable[A] = toObservable(IOPool())
    def toObservable(ec: ExecutionContext): Observable[A] = Observable.apply[A](s => {
      ec.execute(() => {
        s.onNext($.unsafePerformSync)
      })
    }).observeOn(ExecutionContextScheduler.apply(ec))
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
