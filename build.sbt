name := """bdsm"""

version := "1.0"

scalaVersion := "2.11.6"
val akkaVersion = "2.3.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,

  "io.kamon" %% "kamon-core" % "0.3.5",
  "io.kamon" %% "kamon-statsd" % "0.3.5",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")

javaOptions in run ++= Seq("-javaagent:./lib/aspectjweaver-1.8.5.jar")

enablePlugins(JavaAppPackaging)

fork in run := true

fork in run := true