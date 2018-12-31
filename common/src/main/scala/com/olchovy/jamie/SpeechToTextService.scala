package com.olchovy.jamie

import java.io.InputStream
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import com.google.api.gax.rpc.{
  ClientStream,
  ResponseObserver,
  StreamController
}
import com.google.cloud.speech.v1.{
  RecognitionAudio,
  RecognitionConfig,
  SpeechClient,
  StreamingRecognitionConfig,
  StreamingRecognizeRequest,
  StreamingRecognizeResponse
}
import com.google.protobuf.ByteString
import com.olchovy.util.BackgroundResourceUtils

object SpeechToTextService {

  val DefaultEncoding = RecognitionConfig.AudioEncoding.FLAC

  val DefaultLanguageCode = "en-US"

  val DefaultSampleRate = 44100

  def stream(stream: InputStream)(implicit ec: ExecutionContext): Future[Seq[Transcript]] = {
    val clientOrError = Try(SpeechClient.create())
    val future = for {
      client <- Future.fromTry(clientOrError)
      recognitionConfig = RecognitionConfig.newBuilder()
        .setEncoding(DefaultEncoding)
        .setLanguageCode(DefaultLanguageCode)
        .setSampleRateHertz(DefaultSampleRate)
        //.setEnableSpeakerDiarization(true) // not yet available in v1 (try v1p1beta?)
        //.setDiarizationSpeakerCount(2)
        .setEnableAutomaticPunctuation(true)
        .setEnableWordTimeOffsets(true)
        .build()
      streamingConfig = StreamingRecognitionConfig.newBuilder()
        .setConfig(recognitionConfig)
        .build()
      responseObserver = new ResponseObserver[StreamingRecognizeResponse] {
        val promise = Promise[Seq[StreamingRecognizeResponse]]
        val buffer = Seq.newBuilder[StreamingRecognizeResponse]
        def future = promise.future
        def onStart(controller: StreamController): Unit = {}
        def onResponse(response: StreamingRecognizeResponse): Unit = buffer += response
        def onError(err: Throwable): Unit = promise.failure(err)
        def onComplete(): Unit = promise.success(buffer.result)
      }
      callable = client.streamingRecognizeCallable()
      clientStream = callable.splitCall(responseObserver)
      _ <- Future.fromTry(transmitAudioStream(streamingConfig, clientStream, stream))
      responses <- responseObserver.future
    } yield for {
      response <- responses
      result <- response.getResultsList.asScala if result.getIsFinal
      alternative = result.getAlternativesList.get(0)
    } yield {
      val text = alternative.getTranscript
      val words = alternative.getWordsList.asScala.foldLeft(List.empty[Transcript.WordOccurrence]) { (acc, wordInfo) =>
        val word = wordInfo.getWord
        val startTime = wordInfo.getStartTime
        val seconds = startTime.getSeconds
        val nanos = startTime.getNanos
        val millis = secondsAndNanosToMillis(seconds, nanos)
        acc match {
          case Nil => Transcript.WordOccurrence(word, millis) :: acc
          case head :: _ => Transcript.WordOccurrence(word, millis - head.relativeOccurrenceMillis) :: acc
        }
      }.reverse
      Transcript(text, words)
    }
    future.onComplete { _ =>
      clientOrError.foreach(BackgroundResourceUtils.blockUntilShutdown(_))
    }
    future
  }

  private def transmitAudioStream(
    config: StreamingRecognitionConfig,
    client: ClientStream[StreamingRecognizeRequest],
    stream: InputStream,
    bufferSize: Int = 128 * 1024
  ): Try[Unit] = {
    for {
      _ <- Try {
        // The first request must **only** contain the audio configuration:
        client.send(
          StreamingRecognizeRequest.newBuilder()
            .setStreamingConfig(config)
            .build()
        )
      }
      buffer = new Array[Byte](bufferSize)
      _ <- Try {
        // Subsequent requests must **only** contain the audio data.
        while (stream.read(buffer) >= 0) {
          client.send(
            StreamingRecognizeRequest.newBuilder()
              .setAudioContent(ByteString.copyFrom(buffer))
              .build()
          )
        }
      }
    } yield {
      client.closeSend()
    }
  }

  private def secondsAndNanosToMillis(numSeconds: Long, numNanos: Int): Long = {
    (numSeconds * 1000L) + (numNanos / 1000000L)
  }
}
