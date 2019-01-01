package com.olchovy.jamie

import java.io.{File, ByteArrayInputStream, FileInputStream, FileOutputStream}
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.http.exp.{Multipart, MultipartDecoder}
import com.twitter.io.{Buf, StreamIO}
import com.twitter.util.Future
import org.json4s._
import org.json4s.native.Serialization
import com.olchovy.util.FutureConverters._

class TranscriptHandler(implicit ece: ExecutionContextExecutor) extends Service[Request, Response] {

  implicit val formats = DefaultFormats

  def apply(request: Request) = {
    withFileUpload(request, param = "audio-upload") { file =>
      val flacFile = File.createTempFile("xcoded-", ".flac")
      for {
        _ <- ScalaFuture.fromTry(AudioTranscodingService.transcode(file, flacFile))
        stream = new FileInputStream(flacFile)
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

  private def withFileUpload(request: Request, param: String)(f: File => Future[Response]): Future[Response] = {
    MultipartDecoder.decode(request).flatMap(_.files.get(param)) match {
      case Some(fileUploads) if fileUploads.nonEmpty =>
        val file = fileUploads.head match {
          case Multipart.InMemoryFileUpload(buf, _, _, _) => fileFromBuf(buf)
          case Multipart.OnDiskFileUpload(file, _, _, _) => file
        }
        f(file)

      case _ =>
        val response = Response(request.version, Status.BadRequest)
        Future.value(response)
    }
  }

  private def fileFromBuf(buf: Buf): File = {
    val bytes = Buf.ByteArray.Shared.extract(buf)
    val file = File.createTempFile("membuf-", ".bin")
    val inputStream = new ByteArrayInputStream(bytes)
    val outputStream = new FileOutputStream(file)
    StreamIO.copy(inputStream, outputStream)
    file
  }
}
