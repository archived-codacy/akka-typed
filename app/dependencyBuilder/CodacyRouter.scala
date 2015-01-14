package dependencyBuilder

import akka.actor._
import akka.routing.{ActorRefRoutee, Router, SmallestMailboxRoutingLogic}
import framework.common.Logger
import framework.common.Logger.LoggerSystem

import scala.collection.immutable

class CodacyRouter[T <: AnyRef](actorCount: Int, className: String, timeout: akka.util.Timeout)(implicit m: Manifest[T]) extends Actor {

  private implicit val loggerSystem = LoggerSystem.DependencyBuilder
  private lazy val system = ActorSystem("codacy")

  var router = {
    val routees = createRoutees(actorCount, className, timeout).map {
      actor =>
        context watch actor
        ActorRefRoutee(actor)
    }
    Router(SmallestMailboxRoutingLogic(), routees)
  }

  def receive = {
    case Terminated(a) =>
      Logger.warn("Actor died: [%s] as [%s]".format(className, m.runtimeClass.getName))
      router = router.removeRoutee(a)
      createRoutees(1, className, timeout).headOption.map {
        actor =>
          Logger.info("Actor reborn: [%s] as [%s]".format(className, m.runtimeClass.getName))
          context watch actor
          router = router.addRoutee(actor)
      }
    case msg =>
      router.route(msg, sender())
  }

  private def createRoutees[A <: AnyRef](actorCount: Int, className: String, timeout: akka.util.Timeout)(implicit m: Manifest[A]): immutable.IndexedSeq[ActorRef] = {
    (1 to actorCount).map {
      i =>
        Logger.trace("Creating actor of type [%s] as [%s]".format(className, m.runtimeClass.getName))
        val r = TypedActor(system).typedActorOf(TypedProps(Reflect(system).actorClassFor(className)).withTimeout(timeout), m.runtimeClass.getName + i)
        TypedActor(system).getActorRefFor(r)
    }
  }
}
