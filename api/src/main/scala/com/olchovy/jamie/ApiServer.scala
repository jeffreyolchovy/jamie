package com.olchovy.jamie

import scala.util.control.NonFatal
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.Method
import org.slf4j.LoggerFactory
import com.olchovy.util.{Args, Server}

case class ApiServer(port: Int) extends Server {

  val service = ExceptionHandlingFilter andThen RoutingService.byMethodAndPathObject {
    case Method.Post -> Root / "example" => new ExampleHandler
  }
}

object ApiServer {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    try {
      val port = Args(args).required("port").toInt
      ApiServer(port).run()
    } catch {
      case NonFatal(e) =>
        log.error("Unexpected exception encountered", e)
        sys.exit(1)
    }
  }
}
