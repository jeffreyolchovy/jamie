package com.olchovy.jamie

import scala.util.control.NonFatal
import com.twitter.finagle.Service
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.io.StreamIO
import com.twitter.util.Future
import org.slf4j.LoggerFactory
import scalatags.Text.all._
import scalatags.Text.tags2.title
import com.olchovy.util.{Args, Server}

case class WebAppServer(port: Int) extends Server {

  import WebAppServer._

  val service = ExceptionHandlingFilter andThen RoutingService.byMethodAndPathObject {
    case Method.Get -> Root => new IndexHandler
    case Method.Get -> Root / "assets" / ("scripts" | "styles") / _ => AssetsHandler
    case Method.Get -> Root / "assets" / ("scripts" | "styles") / "third-party" / _ => AssetsHandler
    case Method.Post -> Root / "api" / "example" => new ExampleHandler
  }
}

object WebAppServer {

  private val log = LoggerFactory.getLogger(getClass)

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

  class IndexHandler extends Service[Request, Response] {
    def apply(request: Request) = {
      val response = Response(request.version, Status.Ok)
      response.write(IndexHandler.ResponseHtmlString)
      Future.value(response)
    }
  }

  object IndexHandler {
    val ResponseHtmlString = "<!DOCTYPE html>" + html(
      head(
        title("Pull that up, Jamie"),
        meta(charset := "utf-8"),
        link(href := "/assets/styles/third-party/flex-layout-attribute_1.0.3.min.css", rel := "stylesheet"),
        link(href := "/assets/styles/main.css", rel := "stylesheet")
      ),
      body(
        tag("main")(
          tag("section")(
            attr("layout") := "column",
            div(
              attr("layout") := "row stretch-stretch",
              div(id := "controls", "Audio input")
            ),
            div(
              attr("layout") := "rows lg-column stretch-stretch",
              div(id := "transcript-pane", "Transcript"),
              div(id := "entity-pane", "Entities")
            )
          )
        ),
        script(src := "/assets/scripts/third-party/zepto-1.2.0.min.js"),
        script(src := "/assets/scripts/main.js"),
        script(
          """
          """.stripMargin.trim
        )
      )
    )
  }

  object AssetsHandler extends Service[Request, Response] {
    def apply(request: Request) = {
      val stream = getClass.getResourceAsStream(request.path)
      val response = Response(request.version, Status.Ok)
      response.withOutputStream(StreamIO.copy(stream, _))
      Future.value(response)
    }
  }
}
