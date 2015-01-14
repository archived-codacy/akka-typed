package dependencyBuilder

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit._

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigObject
import framework.common.Logger
import framework.common.Logger.LoggerSystem
import framework.config.SystemConfiguration

import scala.collection.concurrent
import scala.collection.convert.decorateAsScala._
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

private[this] class Reflection(access: DynamicAccess) extends Extension {
  def actorClassFor[T](fqcn: String)(implicit m: Manifest[T]) = access.getClassFor[T](fqcn).get
}

private[this] object Reflect extends ExtensionId[Reflection] with ExtensionIdProvider {
  override def lookup() = Reflect

  override def createExtension(system: ExtendedActorSystem) = new Reflection(system.dynamicAccess)
}

object ComponentFactory {

  lazy val instance = new ComponentFactory()

  def setComponents(config: ConfigObject): Unit = {
    val keys = config.keySet().iterator
    while (keys.hasNext) {
      val component = keys.next()
      val interface = "codacy.actors.components.%s.interface".format(component)
      val mclass = "codacy.actors.components.%s.class".format(component)

      instance.setComponent(SystemConfiguration.current.getString(interface), SystemConfiguration.current.getString(mclass))
    }
  }

  def apply() = instance
}

class ComponentFactory() {

  val system = ActorFactory.system

  private implicit val loggerSystem = LoggerSystem.DependencyBuilder

  private[this] val classCache: concurrent.Map[String, String] = new ConcurrentHashMap[String, String]().asScala

  private[this] var testMode = true

  def setTestMode(testMode: Boolean) = {
    this.testMode = testMode
  }

  private[dependencyBuilder] def setComponent(t: ClassTag[_], r: ClassTag[_]): Unit = {
    setComponent(t.runtimeClass.getName, r.runtimeClass.getName)
  }

  private[dependencyBuilder] def setComponent(t: ClassTag[_], r: String): Unit = {
    setComponent(t.runtimeClass.getName, r)
  }

  private[dependencyBuilder] def setComponent(key: String, value: String): Unit = {
    if (classCache.contains(key)) {
      classCache.remove(key)
    }
    classCache.put(key, value)
  }

  def getComponent[T <: AnyRef](implicit m: Manifest[T]): T = {
    getComponent[T](m.runtimeClass.getName)
  }

  def getComponent[T <: AnyRef](clazz: String)(implicit m: Manifest[T]): T = {
    if (!testMode && classCache.contains(clazz)) {
      val className = this.classCache.get(clazz).get
      ActorFactory.getActor[T](className)
    } else {
      val className = classCache.getOrElse(clazz, clazz)
      buildComponent[T](className)
    }
  }


  private[this] def buildComponent[T](className: String)(implicit m: Manifest[T]): T = {
    Logger.trace("Creating component of type [%s] as [%s]".format(className, m.runtimeClass.getName))
    Class.forName(className).getConstructor().newInstance().asInstanceOf[T]
  }
}

private[dependencyBuilder] object ActorFactory {

  private implicit val loggerSystem = LoggerSystem.DependencyBuilder

  private[this] val actorCache: concurrent.Map[String, AnyRef] = new ConcurrentHashMap[String, AnyRef]().asScala

  private[dependencyBuilder] def getActor[T <: AnyRef](className: String)(implicit m: Manifest[T]): T = {
    actorCache.synchronized {
      if (!actorCache.contains(m.runtimeClass.getName)) {
        actorCache.put(m.runtimeClass.getName, getComponentActor[T](className))
      }
      actorCache.get(m.runtimeClass.getName).get.asInstanceOf[T]
    }
  }

  lazy val system = ActorSystem("codacy")

  private def getComponentActor[T <: AnyRef](className: String, timeout: akka.util.Timeout = Timeout(Duration(SystemConfiguration.current.timeout, SECONDS)))(implicit m: Manifest[T]): T = {
    Logger.trace("Creating router for type [%s] as [%s]".format(className, m.runtimeClass.getName))

    val actorCount = getNumberOfActors(m)
    if (actorCount > 1) {
      val router: ActorRef = system.actorOf(Props(new CodacyRouter(actorCount, className, timeout)), m.runtimeClass.getName)

      TypedActor(system).typedActorOf(TypedProps(Reflect(system).actorClassFor(className)).withTimeout(timeout), actorRef = router)
    } else {

      Logger.trace("Creating actor of type [%s] as [%s]".format(className, m.runtimeClass.getName))
      TypedActor(system).typedActorOf(TypedProps(Reflect(system).actorClassFor(className)).withTimeout(timeout), m.runtimeClass.getName)
    }
  }

  private def getNumberOfActors[T](implicit m: Manifest[T]): Int = {
    val key = "codacy.actors.components.%s.instances".format(m.runtimeClass.getName.split('.').takeRight(1)(0))
    if (SystemConfiguration.current.hasValue(key)) {
      SystemConfiguration.current.getInt(key)
    }
    else {
      1
    }
  }
}
