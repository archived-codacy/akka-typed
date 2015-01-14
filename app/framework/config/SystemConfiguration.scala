package framework.config

import framework.common.Logger
import framework.common.Logger.LoggerSystem
import scala.collection.mutable

object SystemConfiguration {
  Logger.debug("Creating SystemConfiguration")(LoggerSystem.Framework)
  lazy val current = new SystemConfiguration()
}


class SystemConfiguration(cache: mutable.HashMap[String, Any]) {

  private val timeoutKey = "codacy.actors.timeout"

  def this() = {
    this(new mutable.HashMap[String, Any]())
    cache.put(timeoutKey, 30)
  }

  def timeout: Int = {
    getValue(timeoutKey)
  }

  def addValue(key: String, value: Any) = {
    cache.put(key, value)
  }

  def getObject(key:String) : AnyRef = {
    getValue(key)
  }

  private def getValue[T](key: String): T = {

    if (!cache.contains(key))
      throw new SystemConfigurationException("Unknown key name: %s".format(key))

    cache.get(key).get.asInstanceOf[T]
  }

  def getInt(key: String): Int = {
    getValue(key)
  }

  def getString(key: String): String = {
    getValue(key)
  }

  def getBoolean(key: String): Boolean = {
    getValue(key)
  }

  def hasValue(key:String):Boolean = {
    cache.contains(key)
  }

  def keys = {
    cache.keys
  }
}

class SystemConfigurationException(message: String) extends Exception(message)

