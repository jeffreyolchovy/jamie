package com.olchovy.jamie

import java.io.InputStream
import scala.collection.mutable.Buffer
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;

object SpeechToTextService {

	def apply(stream: InputStream): Unit = {
		val client: SpeechClient = SpeechClient.create()
		val responseObserver: ResponseObserver[StreamingRecognizeResponse] =
			new ResponseObserver[StreamingRecognizeResponse] {
				val responses: Buffer[StreamingRecognizeResponse] = Buffer.empty[StreamingRecognizeResponse]
				def onStart(controller: StreamController): Unit = {}
				def onResponse(response: StreamingRecognizeResponse): Unit = {
					responses += response
				}
        def onComplete(): Unit = {
					for (response <- responses) {
						val result = response.getResultsList.get(0)
						val alternative = result.getAlternativesList.get(0)
						println(s"Transcript: ${alternative.getTranscript}")
					}
				}
				def onError(err: Throwable): Unit = {
					System.err.println(err.getMessage)
				}
			}

 		val clientStream = client.streamingRecognizeCallable().splitCall(responseObserver)

		val recognitionConfig = RecognitionConfig.newBuilder()
			.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
			.setLanguageCode("en-US")
			.setSampleRateHertz(16000)
			.build()

		val streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
			.setConfig(recognitionConfig)
			.build()

		clientStream.send(
			StreamingRecognizeRequest.newBuilder()
				.setStreamingConfig(streamingRecognitionConfig)
				.build()
		)

		val buffer = new Array[Byte](64 * 1024)
		while (stream.read(buffer) > 0) {
			clientStream.send(
				StreamingRecognizeRequest.newBuilder()
					.setAudioContent(ByteString.copyFrom(buffer))
					.build()
			)
		}

    responseObserver.onComplete()
	}
}
