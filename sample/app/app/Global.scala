import controllers.SessionRedirector
import play.api.GlobalSettings
import play.api.Logger
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.http.HeaderNames.EXPIRES
import play.api.http.HeaderNames.PRAGMA
import play.api.mvc.Action
import play.api.mvc.Handler
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.WithHeaders
import utils.SessionConf
import utils.SessionConf.sessions
import utils.SessionConf.xsession

object Global extends GlobalSettings {

  override def onStart(app: play.api.Application) {
    // do nothing
  }

  override def onError(request: RequestHeader, ex: Throwable): Result = {
    Logger.error("FATAL ERROR", ex)
    super.onError(request, ex)
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val sess = xsession(request)
    println("session is new ?" + sess.isNew)
    if (sess.isNew) {
      SessionConf.storeSession(sess)
      Some(SessionRedirector.redirect(request, sess))
    } else {
      super.onRouteRequest(request).map {
        case action: Action[_] => Action(action.parser) { implicit request =>
          action(request) match {
            case res: WithHeaders[_] =>
              SessionConf.storeSession(xsession)
              SessionConf.cleanup
              res.withSession(request.session + (SessionConf.bucket -> xsession.id))
          }
        }
        case other => other
      }
    }
  }
}
