package com.olchovy.jamie

import java.io.File
import scala.sys.process._
import scala.util.{Try, Success, Failure}
import org.slf4j.LoggerFactory

object AudioTranscodingService {

  private val logger = LoggerFactory.getLogger(getClass)

  private val processLogger = ProcessLogger(logger.info(_), logger.error(_))

  val SoxBinary = "sox"

  val SoxiBinary = "soxi"

  val SoxiFileTypeOption = "-t"

  val SoxiChannelsOption = "-c"

  val SoxiBitsPerSampleOption = "-b"

  def transcode(input: File, output: File): Try[Unit] = {
    val inputString = input.getAbsolutePath
    val outputString = output.getAbsolutePath
    val cmd = SoxBinary +: (soxBitsPerSample(16) ++ Seq(inputString, outputString) ++ soxChannels(1))
    val exitCode = cmd !< processLogger
    exitCode match {
      case 0 => Success(Unit)
      case _ => Failure(new RuntimeException("An error was encountered during the audio transcoding process"))
    }
  }

  def getFileType(input: File): Try[String] = {
    executeSoxi(SoxiFileTypeOption, input)
  }

  def getChannels(input: File): Try[Int] = {
    executeSoxi(SoxiChannelsOption, input).map(Integer.parseInt)
  }

  def getBitsPerSample(input: File): Try[Int] = {
    executeSoxi(SoxiBitsPerSampleOption, input).map(Integer.parseInt)
  }

  private def soxChannels(n: Int) = Seq("channels", "1")

  private def soxBitsPerSample(n: Int) = Seq("-b", "16")

  private def executeSoxi(option: String, input: File): Try[String] = {
    val cmd = Seq(SoxiBinary, option, input.getAbsolutePath)
    Try(cmd !!< processLogger).map(_.trim)
  }
}
