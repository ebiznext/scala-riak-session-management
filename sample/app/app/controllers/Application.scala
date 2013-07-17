package controllers

import play.api._
import play.api.mvc._
import utils.SessionConf.xsession

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def set(key:String, value:String) = Action { implicit request =>
    xsession.put(key, value)
    Ok("Store ->" + xsession.get(key))
  }

  def get(key:String) = Action { implicit request =>
    Ok("Retrieved ->" + xsession.get(key))
  }
}