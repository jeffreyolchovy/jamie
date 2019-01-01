package com.olchovy.jamie

case class Transcript(text: String, words: Seq[Transcript.WordOccurrence])

object Transcript {
  case class WordOccurrence(word: String, utteranceMillis: Long)
}
