package sbtgui

import sbt._
import sbt.Keys._
import scala.sys.process.Process

object GuiPlugin extends AutoPlugin {

  object autoImport {
    val npmExecutable = settingKey[File]("Location of npm executable")
    val npmInstall    = taskKey[Unit]("Install the necessary NPM packages")
    val npmUpdate     = taskKey[Unit]("Update the required NPM packages")

    val nodeModulesDirectory    = settingKey[File]("Location of node_modules directory")
    val nodeModulesBinDirectory = settingKey[File]("Location of installed, executable npm packages")

    val jsSourceDirectory         = settingKey[File]("Location of JavaScript sources")
    val jsSourceManifest          = settingKey[Seq[File]]("Ordered set of JavaScript sources that will be concat'ed")
    val stylusSourceDirectory     = settingKey[File]("Location of Stylus sources")
    val cssTargetDirectory        = settingKey[File]("Location of generated CSS resources")
    val jsTargetDirectory         = settingKey[File]("Location of generated JavaScript resources")
  }

  import autoImport._

  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    nodeModulesDirectory := baseDirectory.value / "node_modules",
    nodeModulesBinDirectory := nodeModulesDirectory.value / ".bin",

    npmExecutable := {
      val log = sLog.value
      val output = Process("which npm") !! log
      file(output)
    },
    npmInstall := {
      val log = streams.value.log
      val bin = npmExecutable.value
      val etc = baseDirectory.value / "etc"
      val prefix = nodeModulesDirectory.value.getParentFile
      Process(s"$bin install $etc --no-shrinkwrap --prefix $prefix --loglevel=error") ! log
    },

    jsSourceDirectory     := (sourceDirectory in Compile).value / "javascript",
    stylusSourceDirectory := (sourceDirectory in Compile).value / "stylus",
    jsTargetDirectory     := (resourceManaged in Compile).value / "assets" / "scripts",
    cssTargetDirectory    := (resourceManaged in Compile).value / "assets" / "styles",

    jsSourceManifest := Seq("main.js").map(jsSourceDirectory.value / _),

    update := update.dependsOn(npmInstall).value,
    resourceGenerators in Compile ++= Seq(
      // concat js sources into single source file
      Def.task {
        val log = streams.value.log
        log.info(s"Concating and copying JavaScript sources")
        IO.createDirectory(jsTargetDirectory.value)
        val target = jsTargetDirectory.value / "main.js"
        val bin = nodeModulesBinDirectory.value
        Process("cat " + jsSourceManifest.value.mkString(" ")) #> target ! log
        Seq(target)
      }.taskValue,
      // generate css from stylus sources
      Def.task {
        val log = streams.value.log
        val src = stylusSourceDirectory.value
        val target = cssTargetDirectory.value
        val bin = nodeModulesBinDirectory.value
        val nodeModules = nodeModulesDirectory.value
        log.info(s"Generating CSS from Stylus source files")
        IO.createDirectory(target)
        Process(s"$bin/stylus -c -u ${nodeModules / "nib"} -o $target $src") ! log
        (target ** "*").get
      }.taskValue
    )
  )
}
