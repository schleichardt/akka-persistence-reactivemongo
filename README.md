akka-persistence-reactivemongo
==============================

A very, very experimental module to store Akka Persistence snapshots in MongoDB with ReactiveMongo, needs Play and ReactiveMongo compiled an published for Akka 2.3.0

## Usage

in build.sbt:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "info.schleichardt" %% "akka-persistence-reactivemongo" % "0.1-SNAPSHOT"
```

in your application.conf:

```
akka.persistence.snapshot-store.plugin = "reactivemongo-snapshot-store"
reactivemongo-snapshot-store.mongo.uri="mongodb://localhost:27017"
```
