package com.olchovy.jamie

import scala.concurrent.ExecutionContext
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}

class EntityDetectionServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfterAll {

  implicit val ece = ExecutionContext.global

  override def beforeAll = {
    require(
      sys.env.contains("GOOGLE_APPLICATION_CREDENTIALS"),
      """
      |GOOGLE_APPLICATION_CREDENTIALS must be set and configured to target a 
      |Google Cloud Platform project with the Natural Language API enabled.
      """.stripMargin.trim
    )
  }

  behavior of "EntityDetectionService"

  it should "detect well-known entities in text input" in {
    val input = "The president of the United States of America lives in the White House."
    val expectedEntities = Set("president", "United States of America", "White House")
    for {
      result <- EntityDetectionService(input)
    } yield {
      result.keySet should contain theSameElementsAs expectedEntities
    }
  }
}
