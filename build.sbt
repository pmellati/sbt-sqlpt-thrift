lazy val thriftPlugin = project.in(file("."))
  .settings(
    name := "sbt-sqlpt-thrift",
    organization := "sqlpt",
    version := "0.1-SNAPSHOT",
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    libraryDependencies ++= Seq(
      "com.twitter"  %% "scrooge-generator" % "4.12.0",
      "com.eed3si9n" %% "treehugger"        % "0.4.1",
      "org.specs2"   %% "specs2-core"       % "3.8.5" % "test"),
    resolvers += Resolver.sonatypeRepo("public"),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )