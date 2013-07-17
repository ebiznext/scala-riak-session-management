import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
              "com.basho.riak" % "riak-client" % "1.1.1",
        "com.esotericsoftware.minlog" % "minlog" % "1.2",
          "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-annotations" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-core" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
