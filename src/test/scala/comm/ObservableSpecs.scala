package comm

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import common.AuxSpecs
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import org.scalatest.{AppendedClues, Suite}
import org.scalatest.exceptions.TestFailedException
import rx.lang.scala.Observable
import scalaz.concurrent.Task

trait ObservableSpecs extends AuxSpecs with MoreObservableInstances with ToMoreMonadPlusOps with AppendedClues {self: Suite =>
  // TODO Move to common lib
  def testObservableMultipleValues[A, F: Action](o: Observable[A], expectedValues: Int, exact: Boolean = true)(
      f: => F)(assertionOnFirst: Seq[A] => Unit): Unit = {
    val blockingQueue = new LinkedBlockingQueue[A]
    o.doOnNext(blockingQueue.put).subscribe()
    implicitly[Action[F]].run(f)

    val results = for (_ <- 1 to expectedValues) yield {
      val $ = blockingQueue.poll(1, TimeUnit.SECONDS)
      if ($ == null)
      // TODO smarter stack trace
        throw new TestFailedException("Blocking queue did not return a result", 5)
      $
    }
    if (exact)
      blockingQueue shouldBe 'empty withClue "; Received more events than expected"
    assertionOnFirst(results)
  }
  def testExpectedEvents[A, F: Action](o: Observable[A], exact: Boolean = true)(f: => F)(
      events: Traversable[A]): Unit =
    testObservableMultipleValues(o, events.size, exact)(f)(_.shouldSetEqual(events))
  def testObservableFirstValue[A, F: Action](o: Observable[A])(f: => F)(assertionOnFirst: A => Unit): Unit =
    testObservableMultipleValues(o, 1)(f)(e => assertionOnFirst(e.head))
  def assertMinimumEvents[F: Action](o: Observable[_], expectedValues: Int)(f: => F): Unit =
    testObservableMultipleValues(o, expectedValues)(f)(_ => ())

  trait Action[A] {
    def run(a: => A): Unit
  }
  object Action {
    implicit object UnitEv extends Action[Unit] {
      override def run(a: => Unit): Unit = a
    }
    implicit def TaskEv[A]: Action[Task[A]] = new Action[Task[A]] {
      override def run(a: => Task[A]): Unit = a.unsafePerformSync
    }
  }
}
