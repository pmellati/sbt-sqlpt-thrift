lazy val thriftPlugin = project.in(file("."))
  .settings(
    name := "sbt-sqlpt-thrift",
    organization := "sqlpt",
    version := "0.2-SNAPSHOT",
    description := "Sbt plugin for generating SQLpt table definitions from thrift files.",
    sbtPlugin := true,
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    publishMavenStyle := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None,
    scalaVersion := "2.10.6",
    libraryDependencies ++= Seq(
      "com.twitter"  %% "scrooge-generator" % "4.12.0",
      "com.eed3si9n" %% "treehugger"        % "0.4.1",
      "org.specs2"   %% "specs2-core"       % "3.8.5" % "test"),
    resolvers += Resolver.sonatypeRepo("public"),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )