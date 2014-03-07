organization := "info.schleichardt"

name := "akka-persistence-reactivemongo"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

val AkkaVersion = "2.3.0"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.0-akka-2.3-SNAPSHOT",
  "com.typesafe.akka" %% "akka-persistence-experimental" % AkkaVersion,
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "info.schleichardt" %% "play-2-embed-mongo" % "0.5-SNAPSHOT" % "test" exclude("com.typesafe.play", "play"),
  "com.github.krasserm" %% "akka-persistence-testkit" % "0.2" % "test"
)


