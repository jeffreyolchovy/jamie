package com.olchovy.jamie

import java.net.URL
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try
import com.google.cloud.language.v1.{
  AnalyzeEntitiesRequest,
  AnalyzeEntitiesResponse,
  Document,
  EncodingType,
  Entity,
  EntityMention,
  LanguageServiceClient
}
import com.olchovy.util.BackgroundResourceUtils
import com.olchovy.util.FutureConverters._

object EntityDetectionService {

  private object EntityMetadataKeys {
    val WikipediaUrl = "wikipedia_url"
  }

  def apply(text: String)(implicit ece: ExecutionContextExecutor): Future[Map[String, URL]] = {
    val clientOrError = Try(LanguageServiceClient.create())
    val document = Document.newBuilder()
      .setContent(text)
      .setType(Document.Type.PLAIN_TEXT)
      .build()
    val request = AnalyzeEntitiesRequest.newBuilder()
      .setDocument(document)
      .setEncodingType(EncodingType.UTF16)
      .build()
    val future: Future[Map[String, URL]] = for {
      client <- Future.fromTry(clientOrError)
      response <- client.analyzeEntitiesCallable.futureCall(request)
    } yield (for {
      entity <- response.getEntitiesList.asScala
      name = entity.getName
      entries = entity.getMetadataMap.asScala
      entry <- entries if entries.contains(EntityMetadataKeys.WikipediaUrl)
      (key, value) = entry if key == EntityMetadataKeys.WikipediaUrl
    } yield {
      name -> new URL(value)
    })(scala.collection.breakOut)
    future.onComplete { _ =>
      clientOrError.foreach(BackgroundResourceUtils.blockUntilShutdown(_))
    }
    future
  }
}
