package info.schleichardt.akka.persistence.snapshotstore

import akka.persistence.snapshot.SnapshotStore
import akka.persistence.{SelectedSnapshot, SnapshotSelectionCriteria}
import scala.concurrent.Future
import reactivemongo.api.{MongoConnection, MongoDriver}
import collection.JavaConversions._
import reactivemongo.bson._
import java.io.{ByteArrayInputStream, ObjectInputStream, ObjectOutputStream, ByteArrayOutputStream}
import reactivemongo.bson.buffer.ArrayReadableBuffer
import akka.persistence.SnapshotMetadata
import reactivemongo.api.collections.default.BSONCollection
import concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.MongoConnection.ParsedURI

//TODO: ensureIndex
//TODO: sort Order correct?
class ReactiveMongoSnapshotStore extends SnapshotStore {

  import ReactiveMongoSnapshotStore._

  lazy val config = context.system.settings.config.getConfig("reactivemongo-snapshot-store")
  lazy val driver = new MongoDriver
  lazy val connection = {
    val uri = config.getString("mongo.uri")
    val parsedURI = MongoConnection.parseURI(uri).get
    driver.connection(parsedURI)
  }
  lazy val db = connection.db(config.getString("mongo.db"))
  lazy val collection = db.collection[BSONCollection](config.getString("mongo.collection"))

  override def delete(processorId: String, criteria: SnapshotSelectionCriteria): Unit = throw new Exception("def delete(processorId: String, criteria: SnapshotSelectionCriteria) not implemented")

  override def delete(metadata: SnapshotMetadata): Unit = {
    import ReactiveMongoSnapshotStore.SnapshotMetadataWriter.write
    val query = BSON.write(metadata)
    collection.remove(query)
  }

  override def saved(metadata: SnapshotMetadata): Unit = {}

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    import ReactiveMongoSnapshotStore.SelectedSnapshotWriter.write
    collection.insert(SelectedSnapshot(metadata, snapshot)).map(_ => ())
  }

  override def loadAsync(processorId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    import ReactiveMongoSnapshotStore.SelectedSnapshotReader.read
    import criteria._
    val query = BSONDocument(
      "processorId" -> processorId,
      "sequenceNr" -> BSONDocument("$lte" -> maxSequenceNr),
      "timestamp" -> BSONDocument("$lte" -> maxTimestamp)
    )
    val sort = BSONDocument("sequenceNr" -> -1)
    collection.find(query).sort(sort).one[SelectedSnapshot]
  }
}

object ReactiveMongoSnapshotStore {

  def toBsonBinary(o: Any): BSONBinary = {
    //TODO error handling and close
    val byteArrayOutputStream = new ByteArrayOutputStream
    val out = new ObjectOutputStream(byteArrayOutputStream)
    out.writeObject(o)
    out.close()
    val bin = BSONBinary(ArrayReadableBuffer(byteArrayOutputStream.toByteArray), Subtype.GenericBinarySubtype)
    byteArrayOutputStream.close()
    bin
  }

  def fromBsonBinary(b: BSONBinary): Any = {
    val array = b.value.readArray(b.value.size)
    val bin = new ByteArrayInputStream(array)
    val in = new ObjectInputStream(bin)
    val res = in.readObject()
    in.close()
    bin.close()
    res
  }

  implicit object SelectedSnapshotWriter extends BSONDocumentWriter[SelectedSnapshot] {
    def write(selectedSnapshot: SelectedSnapshot): BSONDocument = {
      SnapshotMetadataWriter.write(selectedSnapshot.metadata) ++
        BSONDocument("snapshot" -> toBsonBinary(selectedSnapshot.snapshot))
    }
  }

  implicit object SelectedSnapshotReader extends BSONDocumentReader[SelectedSnapshot] {
    def read(doc: BSONDocument): SelectedSnapshot = {
      val metadata = SnapshotMetadataReader.read(doc)
      val snapshot = doc.get("snapshot").collect {
        case b: BSONBinary => fromBsonBinary(b)
      }.get
      SelectedSnapshot(metadata, snapshot)
    }
  }

  implicit object SnapshotMetadataWriter extends BSONDocumentWriter[SnapshotMetadata] {
    def write(snapshotMetadata: SnapshotMetadata): BSONDocument = {
      import snapshotMetadata._
      BSONDocument(
        "processorId" -> processorId,
        "sequenceNr" -> sequenceNr,
        "timestamp" -> timestamp
      )
    }
  }

  implicit object SnapshotMetadataReader extends BSONDocumentReader[SnapshotMetadata] {
    def read(doc: BSONDocument): SnapshotMetadata = {
      SnapshotMetadata(
        doc.getAs[String]("processorId").get,
        doc.getAs[Long]("sequenceNr").get,
        doc.getAs[Long]("timestamp").get)
    }
  }
}