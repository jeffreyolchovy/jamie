package com.olchovy.jamie

import java.io.{File, InputStream}
import scala.concurrent.ExecutionContextExecutor
import com.twitter.io.Buf
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import org.json4s._
import org.json4s.native.Serialization
import com.olchovy.util.FutureConverters._

class TranscriptHandler(implicit ece: ExecutionContextExecutor) extends Service[Request, Response] {

  implicit val formats = DefaultFormats

  def apply(request: Request) = {
    val stream: InputStream = ???
    for {
      results <- SpeechToTextService.stream(stream)
      response = Response(request.version, Status.Ok)
    } yield {
      val jsonString = Serialization.write(results)
      response.setContentType("application/json")
      response.setContentString(jsonString)
      response
    }
  }

  private def fileStream(file: File): InputStream = {
    ???
  }

  private def bufStream(buf: Buf): InputStream = {
    ???
  }
}
