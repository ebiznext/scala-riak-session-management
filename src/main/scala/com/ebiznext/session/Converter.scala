package com.ebiznext.session
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import scala.Array.canBuildFrom
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter
import com.fasterxml.jackson.core.`type`.TypeReference
import java.lang.reflect.{ Type, ParameterizedType }

/**
 * Generic Object Converter
 * Binary converter based on Java standard serializer 
 * A performance improvement would be to rely on https://code.google.com/p/kryo/
 * 
 * JSON converter based on jackson scala module
 */
trait Converter[T] {
  def toDomain[T: Manifest](obj: Array[Byte]): T
  def fromDomain[T: Manifest](value: T): Array[Byte]
}

trait BinaryConverter[T] extends Converter[T] {
  def toDomain[T: Manifest](obj: Array[Byte]): T = safeDecode(obj)

  def fromDomain[T: Manifest](value: T): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(new BufferedOutputStream(bos))
    out writeObject (value)
    out close ()
    bos toByteArray ()
  }

  def safeDecode[T: Manifest](bytes: Array[Byte]) = {
    val cl = Option(this.getClass().getClassLoader())
    val cin = cl match {
      case Some(cls) =>
        new CustomObjectInputStream(new ByteArrayInputStream(bytes), cls)
      case None =>
        new ObjectInputStream(new ByteArrayInputStream(bytes))
    }
    val obj = cin.readObject
    cin.close
    obj.asInstanceOf[T]
  }
}

trait JSONConverter[T] extends Converter[T] {
  def toDomain[T: Manifest](bytes: Array[Byte]): T = {
    val x: Option[T] = None
    JacksonConverter.deserialize[T](new String(bytes))
  }

  def fromDomain[T: Manifest](value: T): Array[Byte] = {
    JacksonConverter.serialize(value) map (_.toChar) toCharArray () map (_.toByte)
  }
  object JacksonConverter {
    private val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    def serialize(value: Any): String = {
      val writer = new StringWriter()
      mapper.writeValue(writer, value)
      writer.toString
    }
    def deserialize[T: Manifest](json: String): T = mapper.readValue(json, typeReference[T])
    private[this] def typeReference[T: Manifest] = new TypeReference[T] {
      override def getType : Type = typeFromManifest(manifest[T])
    }

    private[this] def typeFromManifest(m: Manifest[_]): Type = {
      if (m.typeArguments.isEmpty) { m.erasure }
      else new ParameterizedType {
        def getRawType = m.erasure
        def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
        def getOwnerType = null
      }
    }
  }
}


