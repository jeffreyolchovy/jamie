package com.olchovy.jamie

import scala.collection.JavaConverters._
import com.google.cloud.language.v1.{
	AnalyzeEntitiesRequest,
	AnalyzeEntitiesResponse,
	Document,
	EncodingType,
	Entity,
	EntityMention,
	LanguageServiceClient
}

object EntityDetectionService {

	def apply(text: String): Unit = {
    // Instantiate the Language client (com.google.cloud.language.v1.LanguageServiceClient)
		val client = LanguageServiceClient.create()
		val doc = Document.newBuilder()
			.setContent(text)
			.setType(Document.Type.PLAIN_TEXT)
			.build()
		val request = AnalyzeEntitiesRequest.newBuilder()
			.setDocument(doc)
			.setEncodingType(EncodingType.UTF16)
			.build()
		val response = client.analyzeEntities(request)

		// Print the response
		for (entity <- response.getEntitiesList.asScala) {
			println(s"Entity: ${entity.getName()}")
			println(s"Salience: ${entity.getSalience()}")
			println("Metadata: ")
			for (entry <- entity.getMetadataMap.entrySet.asScala) {
				println(s"${entry.getKey}: ${entry.getValue}")
			}
			for (mention <- entity.getMentionsList.asScala) {
				println(s"Begin offset: ${mention.getText.getBeginOffset}")
				println(s"Content: ${mention.getText.getContent}")
				println(s"Type: ${mention.getType}")
			}
		}
	}
}
