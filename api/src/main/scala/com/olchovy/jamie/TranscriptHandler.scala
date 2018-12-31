package com.olchovy.jamie

import java.io.{File, InputStream, ByteArrayInputStream, FileInputStream}
import scala.concurrent.ExecutionContextExecutor
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import com.twitter.io.Buf
import com.twitter.util.Future
import org.json4s._
import org.json4s.native.Serialization
import com.olchovy.util.FutureConverters._

class TranscriptHandler(implicit ece: ExecutionContextExecutor) extends Service[Request, Response] {

  implicit val formats = DefaultFormats

  def apply(request: Request) = {
    withFileUpload(request, param = "audio-upload") { stream =>
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
  }

  private def withFileUpload(request: Request, param: String)(f: InputStream => Future[Response]): Future[Response] = {
    MultipartDecoder.decode(request).flatMap(_.files.get(param)) match {
      case Some(fileUploads) if fileUploads.nonEmpty =>
        val stream = fileUploads.head match {
          case Multipart.InMemoryFileUpload(buf, _, _, _) => streamFromBuf(buf)
          case Multipart.OnDiskFileUpload(file, _, _, _) => streamFromFile(file)
        }
        f(stream)

      case _ =>
        val response = Response(request.version, Status.BadRequest)
        Future.value(response)
    }
  }

  private def streamFromBuf(buf: Buf): InputStream = {
    val bytes = Buf.ByteArray.Shared.extract(buf)
    new ByteArrayInputStream(bytes)
  }

  private def streamFromFile(file: File): InputStream = {
    new FileInputStream(file)
  }
}
