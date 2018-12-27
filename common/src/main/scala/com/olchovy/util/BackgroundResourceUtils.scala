package com.olchovy.util

import java.util.concurrent.TimeUnit
import com.google.api.gax.core.BackgroundResource
import org.slf4j.LoggerFactory

object BackgroundResourceUtils {

  val DefaultBackoffMillis = 1 * 1000L // 1 second

  val DefaultMaxBackoffMillis = 60 * 1000L // 1 minute

  private val logger = LoggerFactory.getLogger(getClass)

  def blockUntilShutdown(
    resource: BackgroundResource,
    minBackoffMillis: Long = DefaultBackoffMillis,
    maxBackoffMillis: Long = DefaultMaxBackoffMillis,
    maxAttempts: Int = -1,
    forceShutdown: Boolean = false
  ): Unit = {
    if (!resource.isShutdown()) {
      resource.shutdown()
    }
    try {
      var attempt = 0
      var backoffMillis = minBackoffMillis
      while (!resource.isTerminated && (maxAttempts < 0 || attempt < maxAttempts)) {
        logger.info(s"Waiting $backoffMillis millisecond(s) for outstanding tasks to shutdown")
        resource.awaitTermination(backoffMillis, TimeUnit.MILLISECONDS)
        backoffMillis = math.min(2 * backoffMillis, maxBackoffMillis)
        attempt += 1
      }
      if (resource.isTerminated) {
        logger.info(s"$resource shutdown")
      } else if (forceShutdown) {
        logger.info(s"$resource has not yet shutdown, and will be forcibly terminated")
        resource.shutdownNow()
      } else {
        logger.warn(s"$resource has not yet shutdown, and will not be forcibly terminated")
      }
    } catch {
      case e: InterruptedException =>
        logger.warn(s"$resource was interrupted during shutdown attempt and will be forcibly terminated")
        resource.shutdownNow()
        Thread.currentThread.interrupt()
        throw e
    }
  }
}
