package com.ebiznext.session

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import com.google.common.io.Files

@RunWith(classOf[JUnitRunner])
class SessionTest extends FlatSpec with ShouldMatchers {
  val sessionManager2 = new RiakBackend("127.0.0.1", 8098, "SESSION", 20) with JSONConverter[Session] with SessionHandler
  val sessionManager = new FileBackend(Files.createTempDir(), "SESSION", 20) with JSONConverter[Session] with SessionHandler

  "calling addNewSession" should "create a new session" in {
    val session = sessionManager.addNewSession
    session.put("attr1","val1")
    sessionManager.storeSession(session)
    sessionManager.loadSession(session.id)
    assert(sessionManager.loadSession(session.id) match {
      case Some(obj) => true
      case None => false
    })
  }
}