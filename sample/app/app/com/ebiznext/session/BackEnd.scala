package com.ebiznext.session

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import com.basho.riak.client.raw.http.HTTPClientConfig
import com.basho.riak.client.raw.http.HTTPRiakClientFactory
import java.io.PrintWriter
import com.basho.riak.client.query.indexes.BinIndex
import com.basho.riak.client.raw.query.indexes.BinRangeQuery
import com.basho.riak.client.raw.http.HTTPRiakClientFactory
import com.basho.riak.client.raw.http.HTTPClientConfig
import scala.concurrent.ops.spawn

/**
 * Backend trait that describe how array of byte are stored. 
 * and delete objects after their lifetime expires.
 */
trait Backend {
  def delete(key: String)
  def load(key: String): Option[Array[Byte]]
  def store(key: String, value: Array[Byte])
  def processExpires(): Boolean = false
  // Check every minute for object passive lifetime elapsed
  spawn {
    while (processExpires()) {
      Thread.sleep(60 * 1000)

    }
  }

}


/**
 * CRUD on a RIAK Backend.
 * Secondary indexes are used to search for objects and check their passive duration.
 * This requires you to use the LevelDB backend
 * 
 */
class RiakBackend(riakHost: String, riakPort: Int, bucket: String, sessionDuration: Int) extends Backend {
  val riakApi = new RiakApi(HTTPRiakClientFactory.getInstance().newClient(new HTTPClientConfig.Builder().withHost(riakHost).withPort(riakPort).build))
  def load(key: String): Option[Array[Byte]] = riakApi.load(bucket, key)
  def store(key: String, value: Array[Byte]): Unit = riakApi.save(bucket, key, value, Map(), Map("lastModified" -> System.currentTimeMillis().toString))
  def delete(key: String) = riakApi.delete(bucket, key)
  override def processExpires(): Boolean = {
    val olderThan = System.currentTimeMillis() - (sessionDuration * 1000)
    val b_index = BinIndex.named("lastModified");
    val q = new BinRangeQuery(b_index, bucket, "0", olderThan.toString);
    // On les supprime dans la foulée. 
    val results = riakApi.fetchIndex(q).foreach { x => riakApi.delete(bucket, x) }
    true
  }
}

/**
 * CRUD on a file backend.
 * Objects are stored as app/key/values.
 * app-key refers to the filename
 * object is stored inside the file
 * Expiring an object requires to remove delete the file 
 */
class FileBackend(folder: File, bucket: String, sessionDuration: Int) extends Backend {
  def filename(bucket: String, key: String) = s"$bucket-$key"
  def delete(key: String) = new File(filename(bucket, key)).delete()
  override def processExpires(): Boolean = {
    val olderThan = System.currentTimeMillis() - (sessionDuration * 1000)
    val b_index = BinIndex.named("lastModified")
    // Les sessions étant stockées dans des fichiers, on vérifie la date de dernière modification du fichier 
    // On le supprime si antérieur à 20mn.
    folder.listFiles().foreach { f => if (olderThan > f.lastModified() && f.getName().startsWith(bucket)) f.delete() }
    true
  }
  def load(key: String): Option[Array[Byte]] = {
    val sessionFile = new FileInputStream(new File(folder, filename(bucket, key)))
    try {
      val buffer = new BufferedInputStream(sessionFile)
      val input = new ObjectInputStream(buffer)
      Some(input.readObject().asInstanceOf[Array[Byte]])
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        None
    } finally {
      sessionFile.close();
    }
  }

  def store(key: String, data: Array[Byte]): Unit = {
    val sessionFile = new File(folder, filename(bucket, key))
    val out = new FileOutputStream(sessionFile)
    val buffer = new BufferedOutputStream(out);
    val output = new ObjectOutputStream(buffer);
    try {
      output.writeObject(data);
    } finally {
      output.close();
    }
  }
}
