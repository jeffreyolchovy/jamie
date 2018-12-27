package com.olchovy.util

import org.scalatest.FlatSpec

class ArgsSpec extends FlatSpec {

  behavior of "Args"

  it should "provide access to required arguments" in {
    val input = "--key value"
    val args = Args.fromString(input)
    assert(args.required("key") == "value")
  }

  it should "provide access to flag-style arguments" in {
    val input = "--key"
    val args = Args.fromString(input)
    assert(args.boolean("key"))
  }

  it should "provide access to arguments with multiple values" in {
    val input = "--key value1 --key value2"
    val args = Args.fromString(input)
    assert(args.list("key") == Seq("value1", "value2"))
  }
}
