package com.olchovy.jamie

import java.io.InputStream
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.control.NonFatal
import com.twitter.finagle.Service
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.io.StreamIO
import com.twitter.util.Future
import org.slf4j.LoggerFactory
import com.olchovy.util.{Args, Server}

case class WebAppServer(port: Int)(implicit ece: ExecutionContextExecutor) extends Server {

  import WebAppServer._

  val service = ExceptionHandlingFilter andThen RoutingService.byMethodAndPathObject {
    case Method.Get -> Root => IndexHandler
    case Method.Get -> Root / "assets" / ("scripts" | "styles") / _ => AssetsHandler
    case Method.Get -> Root / "assets" / ("scripts" | "styles") / "third-party" / _ => AssetsHandler
    case Method.Get -> Root / "api" / "entities" => new EntitiesHandler
    case (Method.Get | Method.Post) -> Root / "api" / "transcript" => new TranscriptHandler
  }
}

object WebAppServer {

  private val log = LoggerFactory.getLogger(getClass)

  implicit val ece = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(sys.runtime.availableProcessors))

  def main(args: Array[String]): Unit = {
    try {
      val port = Args(args).required("port").toInt
      WebAppServer(port).run()
    } catch {
      case NonFatal(e) =>
        log.error("Unexpected exception encountered", e)
        sys.exit(1)
    }
  }

  private def respondWithStream(response: Response, stream: InputStream): Future[Response] = {
    response.withOutputStream(StreamIO.copy(stream, _))
    Future.value(response)
  }

  object IndexHandler extends Service[Request, Response] {
    def apply(request: Request) = {
      val response = Response(request.version, Status.Ok)
      val stream = getClass.getResourceAsStream("/html/index.html")
      respondWithStream(response, stream)
    }
  }

  object AssetsHandler extends Service[Request, Response] {
    def apply(request: Request) = {
      val response = Response(request.version, Status.Ok)
      val stream = getClass.getResourceAsStream(request.path)
      respondWithStream(response, stream)
    }
  }
}
