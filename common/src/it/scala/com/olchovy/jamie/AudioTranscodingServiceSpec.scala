package com.olchovy.jamie

import java.io.File
import org.scalatest.{FlatSpec, Matchers, TryValues}

class AudioTranscodingServiceSpec extends FlatSpec with Matchers with TryValues {

  behavior of "AudioTranscodingService"

  it should "transcode audio to 16-bit mono" in {
    val inputUrl = getClass.getClassLoader.getResource("audio/p1219-clip-1.mp3")
    val inputFile = new File(inputUrl.toURI)
    val outputFile = File.createTempFile("p1219-clip-1-mono_", ".flac")
    val result = AudioTranscodingService.transcode(inputFile, outputFile)
    result shouldBe 'isSuccess
    // verify input file info
    AudioTranscodingService.getFileType(inputFile).success.value shouldBe "mp3"
    AudioTranscodingService.getChannels(inputFile).success.value should be (2)
    AudioTranscodingService.getBitsPerSample(inputFile).success.value should be (0)
    // verify output file info
    AudioTranscodingService.getFileType(outputFile).success.value shouldBe "flac"
    AudioTranscodingService.getChannels(outputFile).success.value should be (1)
    AudioTranscodingService.getBitsPerSample(outputFile).success.value should be (16)
  }
}
