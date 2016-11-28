lazy val root = (project in file("."))
  .settings(SqlptThriftPlugin.projectSettings)
  .settings(
    version := "0.1",
    scalaVersion := "2.11.8",
    resolvers += Resolver.bintrayIvyRepo("pmellati", "maven"),
    libraryDependencies += "sqlpt" %% "sqlpt" % "0.1.1",
    sqlpt.thriftDir := sourceDirectory {_ / "main" / "thrift" / "sqlpt"}.value,
    sqlpt.tableInfos := {case structName => sqlpt.TableInfo(structName, structName)},

    // Note: Ensure to call compile first, so that the files are generated.
    TaskKey[Unit]("testGeneratedFileContents") := {
      val carsFileContent = IO.read(sqlpt.outputDir.value / "cars.scala")

      val expectedCarsFileContent = """
        |package myorg.cars
        |
        |import sqlpt.api._
        |
        |object Car extends TableDef {
        |  override def name = "Car"
        |  case class Columns(car_id: Column[Str] = "car_id", model: Column[Str] = "model", price: Column[Num] = "price", preowned: Column[Nullable[Bool]] = "preowned")
        |  override def cols = Columns()
        |}""".stripMargin

      if (carsFileContent.trim != expectedCarsFileContent.trim)
        sys.error(s"The contents of the cars.scala file was not as expected:\n\n" +
          s"Expected:\n'${expectedCarsFileContent.trim}'\n\n" +
          s"Actual:\n'${carsFileContent.trim}'")

      val icecreamsFileContent = IO.read(sqlpt.outputDir.value / "icecreams.scala")

      val expectedIcecreamsFileContent = """
        |package myorg.icecreams
        |
        |import sqlpt.api._
        |
        |object Icecream extends TableDef {
        |  override def name = "Icecream"
        |  case class Columns(size: Column[Num] = "size", has_chocolate: Column[Bool] = "has_chocolate", num_calories: Column[Nullable[Num]] = "num_calories")
        |  override def cols = Columns()
        |}""".stripMargin

      if (icecreamsFileContent.trim != expectedIcecreamsFileContent.trim)
        sys.error(s"The contents of the icecreams.scala file was not as expected:\n\n" +
          s"Expected:\n'${expectedIcecreamsFileContent.trim}'\n\n" +
          s"Actual:\n'${icecreamsFileContent.trim}'")
    }
  )