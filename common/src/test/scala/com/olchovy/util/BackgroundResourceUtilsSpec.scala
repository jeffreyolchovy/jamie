package com.olchovy.util

import java.util.concurrent.{Executors, RejectedExecutionException}
import com.google.api.gax.core.ExecutorAsBackgroundResource
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

class BackgroundResourceUtilsSpec extends FlatSpec with Matchers {

  private val logger = LoggerFactory.getLogger(getClass)

  behavior of "BackgroundResourceUtils.blockUntilShutdown"

  it should "await indefinitely until graceful shutdown" in {
    val executor = Executors.newSingleThreadExecutor
    val resource = new ExecutorAsBackgroundResource(executor)
    val future = executor.submit(eventuallyTerminateRunnable(numSeconds = 5))
    BackgroundResourceUtils.blockUntilShutdown(
      resource,
      minBackoffMillis = 1000L,
      maxBackoffMillis = 10 * 1000L,
      maxAttempts = -1,
      forceShutdown = false
    )
    // resource should be shutdown and all tasks terminated
    resource shouldBe 'isShutdown
    resource shouldBe 'isTerminated
    // try to schedule something else
    intercept[RejectedExecutionException] {
      executor.execute(awaitIndefinitelyRunnable)
    }
  }

  it should "await after a certain number of attempts before forcing shutdown" in {
    val executor = Executors.newSingleThreadExecutor
    val resource = new ExecutorAsBackgroundResource(executor)
    executor.execute(awaitIndefinitelyRunnable)
    BackgroundResourceUtils.blockUntilShutdown(
      resource,
      minBackoffMillis = 100L,
      maxBackoffMillis = 1000L,
      maxAttempts = 3,
      forceShutdown = true
    )
    // resource should be shutdown, but all tasks did not terminate as part of orderly shutdown
    resource shouldBe 'isShutdown
    resource shouldBe 'isTerminated
  }

  it should "await after a certain number of attempts before allowing execution to continue" in {
    val executor = Executors.newSingleThreadExecutor
    val resource = new ExecutorAsBackgroundResource(executor)
    val future = executor.submit(awaitIndefinitelyRunnable)
    BackgroundResourceUtils.blockUntilShutdown(
      resource,
      minBackoffMillis = 100L,
      maxBackoffMillis = 1000L,
      maxAttempts = 3,
      forceShutdown = false
    )
    // resource should be shutdown, but not all tasks terminated
    resource shouldBe 'isShutdown
    resource should not be 'isTerminated
    // cancel outstanding task
    future.cancel(true)
  }

  val awaitIndefinitelyRunnable = new Runnable {
    override def run(): Unit = {
      try {
        while (true) {
          logger.debug(s"${Thread.currentThread.getId} is running...")
          Thread.sleep(1000L)
        }
      } catch {
        case e: InterruptedException => Thread.currentThread.interrupt()
      }
    }
  }

  def eventuallyTerminateRunnable(numSeconds: Int) = new Runnable {
    val maxAttempts = numSeconds
    private var attempt = 0
    override def run(): Unit = {
      try {
        while (attempt < maxAttempts) {
          logger.debug(s"${Thread.currentThread.getId} is running...")
          Thread.sleep(1000L)
          attempt += 1
        }
      } catch {
        case e: InterruptedException => Thread.currentThread.interrupt()
      }
    }
  }
}
