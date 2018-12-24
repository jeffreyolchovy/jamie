package com.olchovy.jamie

import com.twitter.finagle.{Http, Filter, Service}
import com.twitter.finagle.http.{ Request, Response, Status}
import org.slf4j.LoggerFactory
import com.twitter.util.Future

object ExceptionHandlingFilter extends Filter[Request, Response, Request, Response] {

  private val log = LoggerFactory.getLogger(getClass)

  override def apply(request: Request, continue: Service[Request, Response]): Future[Response] = {
    continue(request).rescue {
      case e: Throwable =>
        val msg = s"An unexpected error was encountered when processing your request"
        log.error(msg, e)
        val response = Response(request.version, Status.InternalServerError)
        response.contentString = s"$msg: ${e.getMessage}"
        Future.value(response)
    }
  }
}
