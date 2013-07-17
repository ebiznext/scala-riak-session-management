package utils {

  import com.ebiznext.session.FileBackend
  import com.ebiznext.session.JSONConverter
  import com.ebiznext.session.Session
  import com.ebiznext.session.SessionHandler
  import com.google.common.io.Files
  import play.cache.Cache
  import play.api.mvc.RequestHeader
  import java.io.File

  object SessionConf {
    // Get env value from xsession.conf
    def env(key: String): String = play.Play.application.configuration.getConfig("xsession").getString(key)
    // idenitfiant sous lequel la session sera stockée (équivalent à ASPSESSION)
    lazy val bucket: String = env("name")

    // we are using a File backend with sessions stored in JSON format
    lazy val sessions = new FileBackend(new File(env("backend.folder")), SessionConf.bucket, 20) with JSONConverter[Session] with SessionHandler


    def storeSession(xsession: Session) {
      sessions.storeSession(xsession)
    }

    def xsession(implicit request: RequestHeader): Session = {
      xsession(request.session.get(bucket))
    }

    def xsession(idSession: Option[String]): Session = {
      loadSession(idSession)
    }

    // Remove from Play cache at end of request
    def cleanup(implicit request: RequestHeader) = {
      val sessionId = request.session.get(bucket).map(identity)
      sessionId map (Cache.set(_, null, 0))
    }

    // Load sesion from backned if not in cache
    def loadSession(sessionId: Option[String]): Session = {
      val sessionInCache = sessionId flatMap { x =>
        val data = Cache.get(x).asInstanceOf[Session]
        if (data == null) None else Some(data)
      }

      sessionInCache.getOrElse {
        val newSession = sessionId flatMap (sessions.loadSession(_)) getOrElse (sessions.addNewSession())
        Cache.set(newSession.id, newSession, 0)
        newSession
      } access
    }
  }
}