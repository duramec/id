import sbt._
import Keys._

object Settings {
  val jvmVersion        = "1.7"
  val buildOrganization = "com.duramec"
  val buildVersion      = "0.1.2"
  val buildScalaVersion = Version.Scala
  val warnDeadCode      = false
  val buildSettings = Defaults.defaultSettings ++ Seq(
      organization       := buildOrganization,
      version            := buildVersion,
      scalaVersion       := buildScalaVersion,
      scalaBinaryVersion <<= scalaVersion.identity
   )
  
  import Resolvers._
   
  val defaultSettings = buildSettings ++ Seq(
    resolvers ++= Seq(
        typesafeRepo,
        snapshotRepo,
        mavenLocalRepo),
    compileOrder in Compile := CompileOrder.JavaThenScala,
    compileOrder in Test := CompileOrder.Mixed,
    scalacOptions in (Compile, doc) ++= Seq(
        ("-target:jvm-" + jvmVersion),
        "-optimise",
        "-feature",
        "-deprecation",
        "-Ystruct-dispatch:invoke-dynamic",
        "-Yinline",
        "-Yclosure-elim",
        (if (warnDeadCode) "-Ywarn-dead-code" else "")
      ),
    javacOptions ++= Seq(
        "-source", jvmVersion,
        "-target", jvmVersion,
        "-deprecation"
      ),
    javacOptions in doc := Seq("-source", jvmVersion),
    parallelExecution in Test := false,
    publishMavenStyle := true,
    crossPaths := false // disable version number in artifacts
  )
}
    
object Resolvers {
  val typeSafePrefix = "http://repo.typesafe.com/typesafe"
  val typesafeRepo   = "typesafe" at (typeSafePrefix + "/releases/")
  val snapshotRepo   = "snapshot" at (typeSafePrefix + "/snapshots/")
  val mavenLocalRepo = "maven-local" at "file://"+Path.userHome+"/.m2/repository"
}

object Dependencies {
  import Dependency._
  
  val core = Seq(
      jodaTime,
      duraTime,
      scalaTest)
}

object Version {
  val Scala     = "2.10.1"
  val ScalaTest = "2.0.M5b"
}

object Dependency {
  import Version._
  
  val jodaTime     = "joda-time"     % "joda-time"      % "2.1"     % "compile"
  val duraTime     = "com.duramec"   % "time"           % "0.1"     % "compile"
  val scalaTest    = "org.scalatest" % "scalatest_2.10" % ScalaTest % "test"
}

object IdBuild extends Build {
  import java.io.File._
  import Settings._
  
  lazy val beam = Project(
    id = "id",
    base = file("."),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.core
      )
    )
}
