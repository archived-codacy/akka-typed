package framework.common

import scala.language.implicitConversions
import org.slf4j.LoggerFactory
import framework.common.Logger.LoggerSystem.LoggerSystem

object Logger {

  object LoggerSystem extends Enumeration {
    type LoggerSystem = Value
    val DependencyBuilder, Codacy, PreCog, Engine, NativeEngine, CodeParser, RepositorySystem,
    RepositoryListener, Framework, SystemInstruments, ControllerInstruments = Value
  }

  def exception(exception: Throwable)(implicit loggerSystem: LoggerSystem) = {
    LoggerFactory.getLogger(loggerSystem.toString).error("Exception information:", exception)
  }

  def isErrorEnabled(implicit loggerSystem: LoggerSystem) = LoggerFactory.getLogger(loggerSystem.toString).isErrorEnabled

  def error(message: String)(implicit loggerSystem: LoggerSystem) = {
    log(LoggerFactory.getLogger(loggerSystem.toString).error)(message, isErrorEnabled(loggerSystem))
  }

  def isWarnEnabled(implicit loggerSystem: LoggerSystem) = LoggerFactory.getLogger(loggerSystem.toString).isWarnEnabled

  def warn(message: String)(implicit loggerSystem: LoggerSystem) = {
    log(LoggerFactory.getLogger(loggerSystem.toString).warn)(message, isWarnEnabled(loggerSystem))
  }

  def isInfoEnabled(implicit loggerSystem: LoggerSystem) = LoggerFactory.getLogger(loggerSystem.toString).isInfoEnabled

  def info(message: String)(implicit loggerSystem: LoggerSystem) = {
    log(LoggerFactory.getLogger(loggerSystem.toString).info)(message, isInfoEnabled(loggerSystem))
  }

  def isDebugEnabled(implicit loggerSystem: LoggerSystem) = LoggerFactory.getLogger(loggerSystem.toString).isDebugEnabled

  def debug(message: String)(implicit loggerSystem: LoggerSystem) = {
    log(LoggerFactory.getLogger(loggerSystem.toString).debug)(message, isDebugEnabled(loggerSystem))
  }

  def isTraceEnabled(implicit loggerSystem: LoggerSystem) = LoggerFactory.getLogger(loggerSystem.toString).isTraceEnabled

  def trace(message: String)(implicit loggerSystem: LoggerSystem) = {
    log(LoggerFactory.getLogger(loggerSystem.toString).trace)(message, isTraceEnabled(loggerSystem))
  }

  private[this] def log(logger: (String => Unit) = println)(message: String, levelEnabled: Boolean): Unit = {
    if (levelEnabled) logger("[%s] :".format(getCaller) + " " + message)
  }

  private def getCaller: String = {
    val caller = Thread.currentThread().getStackTrace()(4)
    val c = caller.getClassName
    val className = c.substring(c.lastIndexOf(".") + 1, c.length())
    val sb = new StringBuilder(5)
    sb.append(className)
    sb.append(".")
    sb.append(caller.getMethodName)
    sb.append("():")
    sb.append(caller.getLineNumber)
    sb.toString()
  }
}
