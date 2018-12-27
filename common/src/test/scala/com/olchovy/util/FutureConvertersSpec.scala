package com.olchovy.util

import org.scalatest.{AsyncFlatSpec, Matchers}
import scala.concurrent.ExecutionContext
import com.google.api.core.SettableApiFuture
import com.twitter.util.{Future => TwitterFuture}

class FutureConvertersSpec extends AsyncFlatSpec with Matchers {

  import FutureConverters._

  implicit val ece = ExecutionContext.global

  behavior of "googleApiToScala"

  it should "provide a conversion between Google RPC API futures and Scala futures" in {
    val future = SettableApiFuture.create[String]()
    future.set("foo")
    future.map {
      _ shouldBe "foo"
    }
  }

  behavior of "twitterToScala"

  it should "provide a conversion between Twitter futures and Scala futures" in {
    val future = TwitterFuture.True
    future.map {
      _ shouldBe true
    }
  }
}
