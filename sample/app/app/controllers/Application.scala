package controllers

import play.api._
import play.api.mvc._
import utils.SessionConf.xsession

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def set = Action { implicit request =>
    xsession.put("MyKey", "MyValue")
    Ok("Store ->" + xsession.get("MyKey"))
  }

  def get = Action { implicit request =>
    xsession.get("MyKey")
    Ok("Retrieved ->" + xsession.get("MyKey"))
  }
}