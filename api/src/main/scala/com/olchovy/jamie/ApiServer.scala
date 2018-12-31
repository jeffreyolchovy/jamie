package com.olchovy.jamie

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.control.NonFatal
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.Method
import org.slf4j.LoggerFactory
import com.olchovy.util.{Args, Server}

case class ApiServer(port: Int)(implicit ece: ExecutionContextExecutor) extends Server {

  import ApiServer._

  val service = ExceptionHandlingFilter andThen RoutingService.byMethodAndPathObject {
    case (Method.Get | Method.Post) -> Root / "transcript" => new TranscriptHandler
    case Method.Get -> Root / "entities" => new EntitiesHandler
  }
}

object ApiServer {

  private val log = LoggerFactory.getLogger(getClass)

  implicit val ece = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(sys.runtime.availableProcessors))

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
