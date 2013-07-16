import sbt._
import sbt.Keys._

object RiaksessionmanagerBuild extends Build {
  lazy val riaksessionmanager = Project(
    id = "riakapi",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "RiakSessionmanager",
      organization := "com.ebiznext",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.2",
      fork := true,
      publishArtifact in Test := false,
      resolvers += "Maven central Repository" at "http://central.maven.org/maven2/",
      resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      resolvers += "CodeHale" at "http://repo.codahale.com",
      resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases",
      resolvers += "spray" at "http://repo.spray.io/",
      resolvers += "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
      publishTo <<= (version) { version: String =>
        val repoInfo = if (version.trim.endsWith("SNAPSHOT"))
          ("snapshots" -> (Path.userHome.asFile.toURI.toURL + ".m2/repository"))
        else
          ("releases" -> (Path.userHome.asFile.toURI.toURL + ".m2/repository"))
        Some(Resolver.url(repoInfo._1, new java.net.URL(repoInfo._2)))
      },
      // add other settings here
      libraryDependencies ++= Seq (
        //"ch.qos.logback" % "logback-classic" % "1.0.7",
        "com.basho.riak" % "riak-client" % "1.1.1",
        "com.esotericsoftware.minlog" % "minlog" % "1.2",
          "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-annotations" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-core" % "2.2.2",
          "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2",
//        "io.spray" %%  "spray-json" % "1.2.5",
//        "com.esotericsoftware.kryo" % "kryo" % "2.20" exclude ("com.esotericsoftware.reflectasm", "reflectasm") exclude ("com.esotericsoftware.minlog", "minlog") exclude ("org.objenesis", "objenesiss")
          // test deps
              "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
              "junit" % "junit" % "4.11" % "test"
        )))
}

