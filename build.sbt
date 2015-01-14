import com.typesafe.sbt.web.SbtWeb
import play.Play.autoImport._
import play.PlayScala
import sbt.Keys._
import sbt._


name := """akka-remote-typed"""

version := "1.0-SNAPSHOT"


scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
