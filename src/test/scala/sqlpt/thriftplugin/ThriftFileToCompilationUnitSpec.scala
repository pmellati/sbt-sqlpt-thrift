package sqlpt.thriftplugin

import org.specs2.mutable.Specification
import com.twitter.scrooge.{ast => thrift}
import treehugger.forest.{PackageDef => CompilationUnit, _}
import testhelpers.Helpers
import ThriftFileToCompilationUnit.ObjectAndTableName

class ThriftFileToCompilationUnitSpec extends Specification with Helpers {
  "ThriftFileToCompilationUnit" should {
    "translate a valid thrift file into a compilation unit" in {
      val thriftDoc = parseThriftDoc("""
        |#@namespace scala myorg.mypackage
        |
        |// random comments
        |
        |struct Car {
        |     1: required string car_id;           // Blah blah
        |     2: required string model;
        |     3: optional i32    price = 333;
        |}
        |
        |struct CarManufacturer {
        |  1: required string name;
        |  2: optional i32    foundedDate;
        |}
        |
      """.stripMargin)

      fileToCu(thriftDoc) must beSuccessfulTry.withValue {generatedCu: Tree =>
        generatedCu must beSameScalaCodeAs(
          """
            |package myorg.mypackage
            |
            |import sqlpt.api._
            |
            |object Car extends TableDef {
            |  override def name = "Car"
            |
            |  case class Columns(
            |    car_id: Column[Str]           = "car_id",
            |    model:  Column[Str]           = "model",
            |    price:  Column[Nullable[Num]] = "price"
            |  )
            |
            |  override def cols = Columns()
            |}
            |
            |object CarManufacturer extends TableDef {
            |  override def name = "CarManufacturer"
            |
            |  case class Columns(
            |    name:        Column[Str]           = "name",
            |    foundedDate: Column[Nullable[Num]] = "foundedDate"
            |  )
            |
            |  override def cols = Columns()
            |}
          """.stripMargin)
      }
    }

    "fail if the thrift file contains invalid types" in {
      val thriftDoc = parseThriftDoc("""
        |#@namespace scala myorg.mypackage
        |
        |struct Car {
        |     1: required string car_id;           // Blah blah
        |     2: required InvalidType model;
        |}
        |
      """.stripMargin)

      fileToCu(thriftDoc) must beFailedTry[CompilationUnit]
        .withThrowable[RuntimeException]("Thrift type .* is not supported as a SQLpt column type.")
    }

    "honor the structNameToObjectAndTableName parameter" in {
      val thriftDoc = parseThriftDoc("""
        |#@namespace scala myorg.mypackage
        |
        |struct Car {
        |     1: required string car_id;
        |}
        |
        |struct CarManufacturer {
        |  1: required string name;
        |}
        |
      """.stripMargin)

      fileToCu(thriftDoc).get must beSameScalaCodeAs("""
        |package myorg.mypackage
        |
        |import sqlpt.api._
        |
        |object Car extends TableDef {
        |  override def name = "Car"
        |
        |  case class Columns(car_id: Column[Str] = "car_id")
        |  override def cols = Columns()
        |}
        |
        |object CarManufacturer extends TableDef {
        |  override def name = "CarManufacturer"
        |
        |  case class Columns(name: Column[Str] = "name")
        |  override def cols = Columns()
        |}
      """.stripMargin)

      val fileToCuWithNameTranslations = ThriftFileToCompilationUnit(structToCc, {
        case "Car"             => ObjectAndTableName(objectName = "Cars", tableName = "cars")
        case "CarManufacturer" => ObjectAndTableName(objectName = "CarManufacturers", tableName = "car_manu")
      })

      fileToCuWithNameTranslations(thriftDoc).get must beSameScalaCodeAs("""
        |package myorg.mypackage
        |
        |import sqlpt.api._
        |
        |object Cars extends TableDef {
        |  override def name = "cars"
        |
        |  case class Columns(car_id: Column[Str] = "car_id")
        |  override def cols = Columns()
        |}
        |
        |object CarManufacturers extends TableDef {
        |  override def name = "car_manu"
        |
        |  case class Columns(name: Column[Str] = "name")
        |  override def cols = Columns()
        |}
      """.stripMargin)

      val fileToCuWithNameTranslationsAndAnOmission = ThriftFileToCompilationUnit(structToCc, {
        case "Car" => ObjectAndTableName(objectName = "Cars", tableName = "cars")
      })

      fileToCuWithNameTranslationsAndAnOmission(thriftDoc).get must beSameScalaCodeAs("""
        |package myorg.mypackage
        |
        |import sqlpt.api._
        |
        |object Cars extends TableDef {
        |  override def name = "cars"
        |
        |  case class Columns(car_id: Column[Str] = "car_id")
        |  override def cols = Columns()
        |}
      """.stripMargin)

      val fileToCuWithEverythingOmitted = ThriftFileToCompilationUnit(structToCc, Map.empty)

      fileToCuWithEverythingOmitted(thriftDoc).get must beSameScalaCodeAs("""
        |package myorg.mypackage
        |
        |import sqlpt.api._
      """.stripMargin)
    }

    "not fail if the thrift document is empty" in pending

    "not fail if there is no namespace declaration in the thrift document" in pending
  }

  private lazy val structToCc = ThriftStructToColumnsCaseClass(identity, identity)

  private lazy val fileToCu = ThriftFileToCompilationUnit(structToCc, {case x => ObjectAndTableName(x, x)})

  private def parseThriftDoc(str: String): thrift.Document =
    thriftParser.document(toReader(str)).get
}
