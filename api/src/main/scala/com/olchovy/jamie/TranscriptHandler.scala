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
      val flacFile = newNonExistentTempFile("xcoded-", ".flac")
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
          case Multipart.InMemoryFileUpload(buf, _, givenName, _) => tmpFileFromBuf(buf, givenName)
          case Multipart.OnDiskFileUpload(file, _, givenName, _) => tmpFileFromFile(file, givenName)
        }
        f(file)

      case _ =>
        val response = Response(request.version, Status.BadRequest)
        Future.value(response)
    }
  }

  private def tmpFileFromFile(inputFile: File, givenName: String): File = {
    val outputFile = newDerivedTempFile(givenName)
    val inputStream = new FileInputStream(inputFile)
    val outputStream = new FileOutputStream(outputFile)
    StreamIO.copy(inputStream, outputStream)
    outputFile
  }

  private def tmpFileFromBuf(buf: Buf, givenName: String): File = {
    val bytes = Buf.ByteArray.Shared.extract(buf)
    val file = newDerivedTempFile(givenName)
    val inputStream = new ByteArrayInputStream(bytes)
    val outputStream = new FileOutputStream(file)
    StreamIO.copy(inputStream, outputStream)
    file
  }

  private def newDerivedTempFile(givenName: String): File = {
    val i = givenName.lastIndexWhere(_ == '.')
    File.createTempFile(givenName.slice(0, i) + "-", givenName.slice(i, givenName.size))
  }

  private def newNonExistentTempFile(prefix: String, suffix: String): File = {
    val file = File.createTempFile(prefix, suffix)
    if (file.exists) {
      file.delete()
    }
    file
  }
}
