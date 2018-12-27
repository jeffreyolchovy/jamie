package com.olchovy.util

import java.lang.reflect.{InvocationHandler, Method, Proxy}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future => ScalaFuture, Promise => ScalaPromise}
import scala.util.{Success, Failure}
import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures, SettableApiFuture}
import com.twitter.util.{Future => TwitterFuture, Promise => TwitterPromise}

object FutureConverters {

  implicit def googleApiToScala[A](apiFuture: ApiFuture[A])(implicit ece: ExecutionContextExecutor): ScalaFuture[A] = {
    val scalaPromise = ScalaPromise[A]
    ApiFutures.addCallback(
      apiFuture,
      new ApiFutureCallback[A] {
        def onSuccess(a: A) = scalaPromise.success(a)
        def onFailure(e: Throwable) = scalaPromise.failure(e)
      },
      ece
    )
    scalaPromise.future
  }

  implicit def scalaToGoogleApi[A](scalaFuture: ScalaFuture[A])(implicit ec: ExecutionContext): ApiFuture[A] = {
    val apiFuture = SettableApiFuture.create[A]
    scalaFuture.onComplete {
      case Success(a) => apiFuture.set(a)
      case Failure(e) => apiFuture.setException(e)
    }
    apiFuture
  }

  implicit def twitterToScala[A](twitterFuture: TwitterFuture[A]): ScalaFuture[A] = {
    val scalaPromise = ScalaPromise[A]
    twitterFuture
      .onSuccess(scalaPromise.success)
      .onFailure(scalaPromise.failure)
    scalaPromise.future
  }

  implicit def scalaToTwitter[A](scalaFuture: ScalaFuture[A])(implicit ec: ExecutionContext): TwitterFuture[A] = {
    val twitterPromise = TwitterPromise[A]
    scalaFuture.onComplete {
      case Success(a) => twitterPromise.setValue(a)
      case Failure(e) => twitterPromise.setException(e)
    }
    twitterPromise
  }
}
