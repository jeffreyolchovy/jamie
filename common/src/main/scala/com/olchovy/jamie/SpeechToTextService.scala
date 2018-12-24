package com.olchovy.jamie

import java.io.InputStream
import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer
import scala.concurrent.{ExecutionContext, Future, Promise}
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.protobuf.ByteString

object SpeechToTextService {

  implicit val ec = ExecutionContext.global

  // non-streaming, synchronous invocation (only for testing...)
  def apply(stream: InputStream): Unit = {
    val client = SpeechClient.create()
    val config = RecognitionConfig.newBuilder()
      .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
      .setLanguageCode("en-US")
      .setSampleRateHertz(44100)
      .setModel("default")
      //.setEnableSpeakerDiarization(true) // not yet available in v1 (try v1p1beta?)
      //.setDiarizationSpeakerCount(2)
      .setEnableAutomaticPunctuation(true)
      .setEnableWordTimeOffsets(true)
      .build()
    val audioBytes = ByteString.readFrom(stream)
    val audio = RecognitionAudio.newBuilder()
      .setContent(audioBytes)
      .build()
    // Use blocking call to get audio transcript
    val response = client.recognize(config, audio)
    val results = response.getResultsList.asScala

    for (result <- results) {
      val alternative = result.getAlternativesList.get(0)
      println(s"Transcript: ${alternative.getTranscript}")
    }
  }

  def stream(stream: InputStream): Unit = {
    val client = SpeechClient.create()

    val recConfig = RecognitionConfig.newBuilder()
      .setEncoding(RecognitionConfig.AudioEncoding.FLAC)
      .setLanguageCode("en-US")
      .setSampleRateHertz(44100)
      .setModel("default")
      //.setEnableSpeakerDiarization(true) // not yet available in v1 (try v1p1beta?)
      //.setDiarizationSpeakerCount(2)
      .setEnableAutomaticPunctuation(true)
      .setEnableWordTimeOffsets(true)
      .build()

    val config = StreamingRecognitionConfig.newBuilder()
      .setConfig(recConfig)
      .build()

    val responseObserver = new ResponseObserver[StreamingRecognizeResponse] {
      val promise = Promise[Seq[StreamingRecognizeResponse]]
      val messages = Buffer.empty[StreamingRecognizeResponse]
      def future = promise.future
      def onStart(controller: StreamController): Unit = {}
      def onResponse(message: StreamingRecognizeResponse): Unit = { messages += message }
      def onError(err: Throwable): Unit = { promise.failure(err) }
      def onComplete(): Unit = { promise.success(messages.toSeq) }
    }

    val callable = client.streamingRecognizeCallable()

    val clientStream = callable.splitCall(responseObserver)

    try {
      // The first request must **only** contain the audio configuration:
      clientStream.send(
        StreamingRecognizeRequest.newBuilder().setStreamingConfig(config).build()
      )

      // Subsequent requests must **only** contain the audio data.
      val buffer = new Array[Byte](128 * 1024)
      while (stream.read(buffer) != -1) {
        clientStream.send(
          StreamingRecognizeRequest.newBuilder()
            .setAudioContent(ByteString.copyFrom(buffer))
            .build()
        )
      }
    } finally {
      clientStream.closeSend()
      client.shutdown()
    }

    for {
      responses <- responseObserver.future
      response <- responses
    } {
      val result = response.getResultsList().get(0)
      val alternative = result.getAlternativesList().get(0)
      println(s"Transcript: ${alternative.getTranscript}")
    }
  }
}
