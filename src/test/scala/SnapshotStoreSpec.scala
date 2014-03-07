package info.schleichardt.akka.persistence.snapshotstore

import akka.actor._
import akka.persistence._
import akka.testkit._

import com.typesafe.config._

import org.scalatest._
import java.util.UUID

case class TestStateClass(foo: String, bar: Int)

class TestProcessor(override val processorId: String, notificationActor: ActorRef) extends Processor {
  var state: TestStateClass = TestProcessor.testState

  def receive = {
    case "snap" =>
      saveSnapshot(state)
      notificationActor ! "snap"
    case s@SnapshotOffer(metadata, offeredSnapshot) =>
      state = offeredSnapshot.asInstanceOf[TestStateClass]
      notificationActor ! state
    case s@SaveSnapshotSuccess(metadata) =>
      notificationActor ! s
    case s@SaveSnapshotFailure(metadata, reason) =>
      notificationActor ! s
    case _ => //ignore
  }
}

object TestProcessor {
  def props(processorId: String, notificationActor: ActorRef) = Props(new TestProcessor(processorId, notificationActor))

  val testState = TestStateClass("foo", 1)
}

trait SnapshotStoreSpec extends SnapshotSpecDetails {
  "A snapshot store" must {
    "asynchronously save and load a snapshot" in {
      val processorId = UUID.randomUUID().toString
      val processor = system.actorOf(TestProcessor.props(processorId, testActor))
      processor ! "snap"
      expectMsg("snap")
      expectMsgType[SaveSnapshotSuccess]
      processor ! PoisonPill
      val processorReincarnation = system.actorOf(TestProcessor.props(processorId, testActor))
      expectMsg(TestProcessor.testState)
    }
    "multiple snapshots and find the right one" in pending
    "delete a snapshot" in pending
    "delete all snapshots matching a criteria" in pending
  }
}


private[snapshotstore] trait SnapshotSpecDetails extends TestKitBase with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  /**
   * override this for your snapshot storage plugin specific configuration
   * @return
   */
  def testConfig: Config

  implicit lazy val system: ActorSystem = ActorSystem("SnapshotSpec", testConfig)

  override protected def afterAll(): Unit = shutdown(system)
}