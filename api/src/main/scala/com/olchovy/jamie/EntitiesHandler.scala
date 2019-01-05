package com.olchovy.jamie

import scala.concurrent.ExecutionContextExecutor
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import org.json4s._
import org.json4s.native.Serialization
import com.olchovy.util.FutureConverters._

class EntitiesHandler(implicit ece: ExecutionContextExecutor) extends Service[Request, Response] {

  implicit val formats = DefaultFormats

  def apply(request: Request) = {
    val text = request.getParam("text")
    for {
      result <- EntityDetectionService(text)
      response = Response(request.version, Status.Ok)
    } yield {
      val jsonString = Serialization.write(
        result.map {
          case (entity, url) => Map("entity" -> entity, "url" -> url.toString)
        }
      )
      response.setContentType("application/json")
      response.setContentString(jsonString)
      response
    }
  }
}
