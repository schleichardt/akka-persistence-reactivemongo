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
  "info.schleichardt" %% "akka-persistence-snapshot-testkit" % "0.1-SNAPSHOT" % "test",
  "com.github.krasserm" %% "akka-persistence-testkit" % "0.2" % "test"
)

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

val githubPath = "schleichardt/akka-persistence-reactivemongo"

pomExtra := (
  <url>https://github.com/{githubPath}</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:{githubPath}.git</url>
      <connection>scm:git:git@github.com:{githubPath}.git</connection>
    </scm>
    <developers>
      <developer>
        <id>schleichardt</id>
        <name>Michael Schleichardt</name>
        <url>http://michael.schleichardt.info</url>
      </developer>
    </developers>
  )