package controllers

import play.api.mvc.Action
import play.api.mvc.RequestHeader
import play.api.mvc.Controller
import com.ebiznext.session.Session
import utils.SessionConf


object SessionRedirector extends Controller {
  /*
   * Instruct a round trip on the original request but this time with the session id.
   */
  def redirect(request: RequestHeader, sess : Session) = Action {
    Redirect(request.path, request.queryString, 301) withSession(request.session + (SessionConf.bucket -> sess.id))
  }
}
