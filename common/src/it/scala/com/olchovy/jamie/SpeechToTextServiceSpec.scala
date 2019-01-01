package com.olchovy.jamie

import scala.concurrent.ExecutionContext
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}

class SpeechToTextServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  implicit val ece = ExecutionContext.global

  override def beforeAll = {
    require(
      sys.env.contains("GOOGLE_APPLICATION_CREDENTIALS"),
      """
      |GOOGLE_APPLICATION_CREDENTIALS must be set and configured to target a 
      |Google Cloud Platform project with the Speech-to-Text API enabled.
      """.stripMargin.trim
    )
  }

  behavior of "SpeechToTextService"

  it should "offer transcriptions of audio streams" in {
    val stream = getClass.getClassLoader.getResourceAsStream("audio/p1219-clip-1-mono.flac")
    for {
      results <- SpeechToTextService.stream(stream)
      result = results.map(_.text).mkString(" ")
    } yield {
      result should (include("rabbit hole") and include("garage") and include("the show"))
    }
  }

  it should "contain word occurrence information for transcribed audio streams" in {
    val stream = getClass.getClassLoader.getResourceAsStream("audio/p1218-clip-1-mono.flac")
    for {
      results <- SpeechToTextService.stream(stream)
    } yield {
      pending
    }
  }
}
