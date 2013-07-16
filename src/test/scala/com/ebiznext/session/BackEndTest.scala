package com.ebiznext.session

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import com.google.common.io.Files

case class BackEndTestData(i: Int, s: String)

@RunWith(classOf[JUnitRunner])
class BackEndTest extends FlatSpec with ShouldMatchers {
  val tempDir = Files.createTempDir()
  val APPLICATION = "TEST_APP"
  val backend = new FileBackend(tempDir, APPLICATION,  1000)
  val riakBackend = new RiakBackend("127.0.0.1", 8098, APPLICATION, 1000)


  "store on Text File backend" should "should increase number of entries" in {
    val size = tempDir.list().size
    backend.store("key1", "hello".map(_.toByte).toArray)
    tempDir.list().size should (equal(size + 1))
  }
  "store on Riak backend" should "run safely" in {
    riakBackend.store("key1", "hello".map(_.toByte).toArray)
    assert(true)
  }
  it should "Load the previously inserted entry" in {
    val value = backend.load( "key1")
    value match {
      case Some(x) => assert(java.util.Arrays.equals(x, "hello".map(_.toByte).toArray))
      case None => assert(false)
    }
  }
}
