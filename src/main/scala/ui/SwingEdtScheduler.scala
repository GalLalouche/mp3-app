package ui

import rx.lang.scala.Scheduler
import rx.lang.scala.schedulers.ExecutionContextScheduler

import scala.concurrent.ExecutionContext
import scala.swing.Swing

object SwingEdtScheduler {
  private lazy val ecs = ExecutionContextScheduler(new ExecutionContext {
    override def execute(runnable: Runnable): Unit = Swing.onEDT(runnable.run())
    override def reportFailure(cause: Throwable): Unit = ???
  })
  def apply(): Scheduler = ecs
}
