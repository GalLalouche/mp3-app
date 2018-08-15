package common

import java.util.concurrent.Executors

import rx.lang.scala.schedulers.ExecutionContextScheduler

import scala.concurrent.ExecutionContext

object IOPool {
  private lazy val executor = Executors.newFixedThreadPool(10)
  def apply(): ExecutionContext = ExecutionContext.fromExecutor(executor)
  def scheduler = ExecutionContextScheduler(apply())
}
