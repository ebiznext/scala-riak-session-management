package utils {

  import com.ebiznext.session.FileBackend
  import com.ebiznext.session.JSONConverter
  import com.ebiznext.session.Session
  import com.ebiznext.session.SessionHandler
  import com.google.common.io.Files
  import play.cache.Cache
  import play.api.mvc.RequestHeader
  import java.io.File

  /*
 * Objet d'accès aux sessions.
 * Les sessions sont stockées dans un cache global sous la forme de clef valeur
 * En début de requête, ces données sont préchargées de la base si elles existent
 * En fin de requête, ces données sont sauvegardées à nouveau poru être récupérées lors du prochain appel..
 */
  object SessionConf {
    // Permet de récupérer les valeurs de configuration pour la xsession
    def env(key: String): String = play.Play.application.configuration.getConfig("xsession").getString(key)
    // idenitfiant sous lequel la session sera stockée (équivalent à ASPSESSION)
    lazy val bucket: String = env("name")

    // DAO d'accès au support de stockage des sessions. Construction de l'objet via le cacke pattern
    // ce qui nosu permet à tout moment de substituer une base par une autre.
    // Deux solutions possibles Riak ou File au moment du commentaire
    lazy val sessions = new FileBackend(new File(env("backend.folder")), SessionConf.bucket, 20) with JSONConverter[Session] with SessionHandler

    // Stockage dela session
    def storeSession(xsession: Session) {
      xsession.attributes.foreach(x => println(x._1 + "=" + x._2))
      sessions.storeSession(xsession)
    }

    // Chargement de la session
    // goodies qui permet de charger la session à partir d'un contrôleur 
    // sans qu'il soit nécessaire d'indiquer la clef de session
    def xsession(implicit request: RequestHeader): Session = {
      println("-> bucket => " + bucket)
      xsession(request.session.get(bucket))
    }

    // goodies de chargement d'une session à partir de l'identifiant
    def xsession(idSession: Option[String]): Session = {
      println("-> xsession(idSession: Option[String]) | idSession => " + idSession)
      loadSession(idSession)
    }

    // En fin de requête on purge du cache statique les données de sessions.
    def cleanup(implicit request: RequestHeader) = {
      val sessionId = request.session.get(bucket).map(identity)
      sessionId map (Cache.set(_, null, 0))
    }

    // Chargement d'une session à partir de la base
    // Noter la délégation à sessions qui permet de s'abstraire de l'impélmentation Fichier ou Base.
    def loadSession(sessionId: Option[String]): Session = {
      println("->LOAD-SESSION sessionId => " + sessionId)
      val sessionInCache = sessionId flatMap { x =>
        val data = Cache.get(x).asInstanceOf[Session]
        if (data == null) None else Some(data)
      }
      println("->sessionInCache => " + sessionInCache)

      sessionInCache.getOrElse {
        val newSession = sessionId flatMap (sessions.loadSession(_)) getOrElse (sessions.addNewSession())
        Cache.set(newSession.id, newSession, 0)
        println("->newSession => " + newSession.isNew)
        newSession
      } access
    }
  }
}