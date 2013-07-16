package com.ebiznext.session
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonIgnore

object SessionEvent extends Enumeration {
  type SessionEvent = Value
  val CREATED, DESTROYED, ACTIVATED, PASSIVATED = Value
}

/*
 *Session Object 
 */
case class Session(
  val creationTime: Long = System.currentTimeMillis,

  // The key
  val id: String = UUID.randomUUID().toString(),

  // Did the user invalidated the session
  var isValid: Boolean = true,

  // Is it just created ?
  var isNew: Boolean = true,

  // Last access time
  var accessed: Long = System.currentTimeMillis,

  // Max inactive interval before the session is expired 
  var maxInactiveInterval: Long = 20) {
  import SessionEvent._
  @JsonIgnore
  var listeners: Map[SessionEvent, (Session) => Unit] = Map()
  // Session object attributes
  var attributes: Map[String, Any] = Map()

  def listen(event: SessionEvent, listener: (Session) => Unit) = listeners += (event -> listener)
  def doNotListen(event: SessionEvent) = listeners -= event
  
  // was the session modified since the last load ?
  var dirty: Boolean = false
  
  //get Atttribute value
  def apply(key: String): Any = attributes(key)
  
  // get attribute value if exist
  def get(key: String): Option[Any] = attributes.get(key)
  
  // set attribute value 
  def put(key: String, value: Any): Unit = synchronized {
    dirty = true
    attributes += (key -> value)
  }
  // Update last access time
  def access(): Session = { this.accessed = System.currentTimeMillis(); this }
  // Clear session
  def clear() = attributes = Map()
}

/**
 * Session handler 
 * require to be composed with a backend and a converter
 * backend for storage and converter for session format
 */
trait SessionHandler { backend: Backend with Converter[Session] =>
  def addNewSession(): Session = {
    val session = new Session()
    storeSession(session)
    session
  }
  def storeSession(session: Session) = {
    backend.store(session.id, backend.fromDomain(session))
  }
  def loadSession(id: String): Option[Session] = {
    val xx = backend.load(id)
    xx.map(backend.toDomain[Session](_))
  }
}


