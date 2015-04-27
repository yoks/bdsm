name := """bdsm"""

version := "1.0"

scalaVersion := "2.11.6"
val akkaVersion = "2.3.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")


fork in run := true

fork in run := true