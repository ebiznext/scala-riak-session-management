package com.ebiznext.session

import java.io.ObjectInputStream
import java.io.ObjectStreamClass
import java.io.InputStream
import scala.Array.canBuildFrom

/**
 * Handle when running thorugh SBT and forking is not activated
 */
class CustomObjectInputStream(in: InputStream, cl: ClassLoader) extends ObjectInputStream(in) {
  override def resolveClass(cd: ObjectStreamClass): Class[_] =
    try {
      cl.loadClass(cd.getName())
    } catch {
      case cnf: ClassNotFoundException =>
        super.resolveClass(cd)
    }
  override def resolveProxyClass(interfaces: Array[String]): Class[_] =
    try {
      val ifaces = interfaces map { iface => cl.loadClass(iface) }
      java.lang.reflect.Proxy.getProxyClass(cl, ifaces: _*)
    } catch {
      case e: ClassNotFoundException =>
        super.resolveProxyClass(interfaces)
    }
}
