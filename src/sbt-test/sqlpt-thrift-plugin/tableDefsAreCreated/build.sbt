lazy val root = (project in file("."))
  .settings(SqlptThriftPlugin.projectSettings)
  .settings(
    version := "0.1",
    scalaVersion := "2.11.8",
    resolvers += Resolver.bintrayIvyRepo("pmellati", "maven"),
    libraryDependencies += "sqlpt" %% "sqlpt" % "0.1.1",
    sqlpt.thriftDir := sourceDirectory {_ / "main" / "thrift" / "sqlpt"}.value

    // Example of using a task to check general purpose things when using scripted.
//    TaskKey[Unit]("check") := {
//      val process = sbt.Process("java", Seq("-jar", (crossTarget.value / "foo.jar").toString))
//      val out = (process!!)
//      if (out.trim != "hello") sys.error("unexpected output: " + out)
//      ()
//    },
  )