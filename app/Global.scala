import dependencyBuilder.ComponentFactory
import framework.common.Logger.LoggerSystem
import play.api.{Application, GlobalSettings}
import rules.ConfigurationInjector

object Global extends GlobalSettings {

  private implicit val loggerSystem = LoggerSystem.Codacy

  override def onStart(app: Application) {
    framework.common.Logger.info("Application has started")

    val conf = app.configuration
    val configInjector = new ConfigurationInjector()
    configInjector.inject(conf.entrySet)
    ComponentFactory.instance.setTestMode(testMode = false)
    ComponentFactory.setComponents(conf.getObject("codacy.actors.components").get)

  }
}
