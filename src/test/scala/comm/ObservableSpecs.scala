package comm

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import common.AuxSpecs
import common.rich.func.{MoreObservableInstances, ToMoreMonadPlusOps}
import org.scalatest.Suite
import org.scalatest.exceptions.TestFailedException
import rx.lang.scala.Observable

trait ObservableSpecs extends AuxSpecs with MoreObservableInstances with ToMoreMonadPlusOps {self: Suite =>
  // TODO Move to common lib
  def testObservableMultipleValues[A](o: Observable[A], expectedValues: Int, exact: Boolean = true)(
      f: => Any)(assertionOnFirst: Seq[A] => Unit): Unit = {
    val blockingQueue = new LinkedBlockingQueue[A]
    o.doOnNext(blockingQueue.put).subscribe()
    f

    val results = for (_ <- 1 to expectedValues) yield {
      val $ = blockingQueue.poll(1, TimeUnit.SECONDS)
      if ($ == null)
      // TODO smarter stack trace
        throw new TestFailedException("Blocking queue did not return a result", 5)
      $
    }
    if (exact)
      blockingQueue shouldBe 'empty
    assertionOnFirst(results)
  }
  def testExpectedEvents[A](o: Observable[A], exact: Boolean = true)(f: => Any)(
      events: Traversable[A]): Unit =
    testObservableMultipleValues(o, events.size, exact)(f)(_.shouldSetEqual(events))
  def testObservableFirstValue[A](o: Observable[A])(f: => Any)(assertionOnFirst: A => Unit): Unit =
    testObservableMultipleValues(o, 1)(f)(e => assertionOnFirst(e.head))
  def assertMinimumEvents(o: Observable[_], expectedValues: Int)(f: => Any): Unit =
    testObservableMultipleValues(o, expectedValues)(f)(_ => ())
}
