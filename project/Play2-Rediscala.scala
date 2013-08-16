import sbt._
import sbt.Keys._

object BuildSettings {
  val buildVersion = "1.0.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "fr.njin",
    version := buildVersion,
    scalaVersion := "2.10.0",
    crossScalaVersions := Seq("2.10.0"),
    crossVersion := CrossVersion.binary
  ) ++ Publish.settings ++
    ScctPlugin.instrumentSettings ++
    com.github.theon.coveralls.CoverallsPlugin.coverallsSettings
}

object Publish {
  object TargetRepository {
    def sonatype: Project.Initialize[Option[sbt.Resolver]] = version { (version: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (version.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  }
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= TargetRepository.sonatype,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/njin-fr/play2-rediscala")),
    pomExtra := (
      <scm>
        <url>git://github.com/njin-fr/play2-rediscala.git</url>
        <connection>scm:git://github.com/njin-fr/play2-rediscala.git</connection>
      </scm>
      <developers>
        <developer>
          <id>dbathily</id>
          <name>Didier Bathily</name>
        </developer>
      </developers>)
  )
}

object RediscalaBuild extends Build {
  import BuildSettings._

  lazy val reactivemongo = Project(
    "Play2-Rediscala",
    file("."),
    settings = buildSettings ++ Seq(
      resolvers := Seq(
        "Sonatype" at "http://oss.sonatype.org/content/groups/public/",
        //"Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
        "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
        "rediscala" at "https://github.com/etaty/rediscala-mvn/raw/master/releases/",
        "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"
      ),
      libraryDependencies ++= Seq(
        "com.etaty.rediscala" %% "rediscala" % "1.0" cross CrossVersion.binary,
        "play" %% "play" % "2.1.0" cross CrossVersion.binary,
        "play" %% "play-test" % "2.1.0" % "test" cross CrossVersion.binary,
        "org.specs2" %% "specs2" % "2.1.1" % "test" cross CrossVersion.binary,
        //https://github.com/mtkopone/scct/issues/54
        "reaktor" %% "scct" % "0.2-SNAPSHOT" % "test"
      )
    )
  )
}
