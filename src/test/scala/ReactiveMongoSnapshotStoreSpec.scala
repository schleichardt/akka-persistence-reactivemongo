package info.schleichardt.akka.persistence.snapshotstore

import com.typesafe.config.{ConfigFactory, Config}
import scala.util.Try


class ReactiveMongoSnapshotStoreSpec extends EmbeddedMongoDB with SnapshotStoreSpec {
  override def testConfig: Config = ConfigFactory.parseString(
    s"""
      | akka.persistence.snapshot-store.plugin = "reactivemongo-snapshot-store"
      | akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      | reactivemongo-snapshot-store.mongo.uri="mongodb://localhost:$port"
    """.stripMargin)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    println(testConfig)
    startMongoDB
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    stopMongoDB
  }
}

//this trait needs to be first mixed in or port == 0
trait EmbeddedMongoDB {

  import de.flapdoodle.embed.mongo.{MongodProcess, MongodExecutable}
  import de.flapdoodle.embed.process.runtime.Network
  import info.schleichardt.play2.embed.mongo.MongoExeFactory

  private var mongoExe: MongodExecutable = _
  private var process: MongodProcess = _

  val port = Network.getFreeServerPort

  def startMongoDB {
    val versionNumber = "2.4.9"
    mongoExe = MongoExeFactory(port, versionNumber)
    process = mongoExe.start()
  }

  def stopMongoDB {
    Try {
      Option(mongoExe).map(_.stop())
    }
    Try {
      Option(process).map(_.stop())
    }
  }
}