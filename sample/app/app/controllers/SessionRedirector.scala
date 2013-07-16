package controllers

import play.api.mvc.Action
import play.api.mvc.RequestHeader
import play.api.mvc.Controller
import com.ebiznext.session.Session
import utils.SessionConf

object SessionRedirector extends Controller {
  def redirect(request: RequestHeader, sess : Session) = Action {
    Redirect(request.path, request.queryString, 301) withSession(request.session + (SessionConf.bucket -> sess.id))
  }
}
