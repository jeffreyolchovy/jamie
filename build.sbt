inThisBuild(
  Seq(
    organization  := "com.olchovy",
    version       := "0.1.0-SNAPSHOT",
    scalaVersion  := "2.12.8",
    scalacOptions := Seq("-deprecation", "-feature", "-language:_")
  )
)

cancelable in Global := true

lazy val root = (project in file(".")).aggregate(common, api, gui)

val common = (project in file("common"))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.json4s" %% "json4s-native" % "3.5.2",
      "com.twitter" %% "finagle-core" % "6.44.0",
      "com.twitter" %% "finagle-http" % "6.44.0",
      "com.google.cloud" % "google-cloud-language" % "1.56.0",
      "com.google.cloud" % "google-cloud-speech" % "0.74.0-beta",
      "io.grpc" % "grpc-netty-shaded" % "1.17.1",
      "io.netty" % "netty-tcnative-boringssl-static" % "2.0.20.Final" classifier "osx-x86_64",
      "org.scalatest" %% "scalatest"  % "3.0.1" % "it,test",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "it,test"
    )
  )

val api = (project in file("api"))
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest"  % "3.0.1" % Test
    ),
    fork in (Compile, run) := true,
    mainClass in (Compile, run) := Some("com.olchovy.jamie.ApiServer")
  )
  .dependsOn(common)

val gui = (project in file("gui"))
  .settings(
    fork in (Compile, run) := true,
    mainClass in (Compile, run) := Some("com.olchovy.jamie.WebAppServer")
  )
  .dependsOn(api)
  .enablePlugins(GuiPlugin)
