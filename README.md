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
Actors are strongly typed and running with several instances, managed by a smallest mailbox routing logic (check CodacyRouter).

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

 - Identify new Static Analysis issues
 - Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
 - Auto-comments on Commits and Pull Requests
 - Integrations with Slack, HipChat, Jira, YouTrack
 - Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
