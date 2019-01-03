package com.olchovy.util

import java.net.InetSocketAddress
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, StorageUnit}
import org.slf4j.LoggerFactory

trait Server {

  private val log = LoggerFactory.getLogger(getClass)

  def port: Int

  def service: Service[Request, Response]

  def run(): Unit = {
    val bindAddress = new InetSocketAddress(port)
    val server = Http.server
      .withMaxRequestSize(StorageUnit.fromMegabytes(256))
      .withMaxResponseSize(StorageUnit.fromMegabytes(32))
      .serve(bindAddress, service)
    try {
      log.info(s"Server listening at $bindAddress")
      sys.addShutdownHook {
        server.close()
      }
      Await.ready(server)
    } catch {
      case e: InterruptedException =>
        log.info("Server interrupted")
        Thread.currentThread.interrupt()
        throw e
    } finally {
      log.info("Server shutting down")
      server.close()
    }
  }
}
