package com.ebiznext.session
import scala.collection.mutable.ListBuffer
import com.basho.riak.client.bucket.FetchBucket
import com.basho.riak.client.builders.RiakObjectBuilder
import com.basho.riak.client.cap.DefaultRetrier
import com.basho.riak.client.raw.RawClient
import com.basho.riak.client.raw.query.indexes.IndexQuery
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

/**
 * Riak Scala API
 * to retrieve keys, fetch objects / remove values / add values
 * Set QORUM to be N/2 +1
 *  
 */
class RiakApi(val rawClient: RawClient) {
  private val retrier = DefaultRetrier.attempts(3)

  def keys(bucketName: String): Array[String] = {
    val iter = rawClient.listKeys(bucketName).iterator()
    val list = ListBuffer[String]()
    while (iter.hasNext()) {
      list += iter.next
    }
    list.toArray
  }
  def delete(bucketName: String, key: String) = rawClient.delete(bucketName, key)
  def clear(bucketName: String) = keys(bucketName) foreach (delete(bucketName, _))
  def save(bucketName: String, key: String, value: Array[Byte], meta: Map[String, String] = Map(), indexes: Map[String, String] = Map()) {
    val bucket = new FetchBucket(rawClient, bucketName, retrier)
    val riakObject = RiakObjectBuilder.newBuilder(bucketName, key).withValue(value).withLastModified(System.currentTimeMillis()).withUsermeta(meta asJava) build ()
    indexes.foreach { case (key, value) => riakObject.addIndex(key, value) }
    rawClient.store(riakObject)
  }
  def fetchIndex(indexQuery: IndexQuery): Iterable[String] = rawClient.fetchIndex(indexQuery) asScala

  def load(bucketName: String, key: String): Option[Array[Byte]] = {
    require(bucketName != null)
    require(key != null)

    val fetched = rawClient.fetch(bucketName, key)
    if (fetched.hasValue())
      Some(fetched.getRiakObjects()(0).getValue())
    else
      None
  }
}
