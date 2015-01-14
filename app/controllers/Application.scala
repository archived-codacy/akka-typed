package controllers

import play.api.mvc._
import dependencyBuilder.ComponentFactory
import framework.component.{IText, IMath}

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def number = Action {
    val generator = ComponentFactory.instance.getComponent[IMath]
    Ok(views.html.index(generator.getNumber.toString))
  }

  def text = Action {
    val generator = ComponentFactory.instance.getComponent[IText]
    Ok(views.html.index(generator.getGreeting))
  }

}