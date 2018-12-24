package com.olchovy.jamie

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.json4s._
import org.json4s.native.Serialization.{read, write}

class ExampleHandler extends Service[Request, Response] {

  implicit val format = DefaultFormats

  def apply(request: Request) = {
    val jsonString = request.getContentString
    /*
    val model = read[???](jsonString)
    val output = write(model)
    */
    val response = Response(request.version, Status.Ok)
    //response.setContentString(output)
    Future.value(response)
  }
}
