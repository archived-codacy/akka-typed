##Typed actors test
---

####Create an actor
To create an actor, simply call ComponentFactory.instance.getComponent[A], where type A will need to be a trait. The system will create the class defined in the config file for that trait (check application.conf).

When called with a class, the component factory will simply instantiate and return that specific type to the caller.

####Dependency injection for unit testing
The system can also be modified to be used to manage dependency injection, for example for unit testing.

For example, add in class ComponentFactory:

    class ComponentFactory {

    ...

      private[this] var injector = Guice.createInjector()

      def setGuiceModule(module: ScalaModule) = {
        this.injector = Guice.createInjector(module)
      }

      //Replace buildComponent with this implementation
      private[this] def buildComponent[T](className: String)(implicit m: Manifest[T]): T = {
        Logger.trace("Creating component of type [%s] as [%s]".format(className, m.runtimeClass.getName))
        injector.getInstance(Class.forName(className)).asInstanceOf[T]
      }
    }

####Typed actors
Actors are strongly typed and running with several instances, managed by a smallest mailbox routing logic (check [CodacyRouter.scala](https://github.com/codacy/akka-typed/blob/master/app/dependencyBuilder/CodacyRouter.scala)).

