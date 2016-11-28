package sqlpt.thriftplugin

import org.specs2.mutable.Specification
import treehugger.forest._
import testhelpers.Helpers
import ThriftFileToCompilationUnit.ObjectAndTableName

class ThriftFileToCompilationUnitSpec extends Specification with Helpers {
  "ThriftFileToCompilationUnit" should {
    "translate a valid thrift file into a compilation unit" in {
      val thriftDocStr =
        """
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
        """.stripMargin

      val thriftDoc = thriftParser.document(toReader(thriftDocStr)).get

      val structToCc = ThriftStructToColumnsCaseClass(identity, identity)

      val fileToCu = ThriftFileToCompilationUnit(structToCc, {case x => ObjectAndTableName(x, x)})

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

    "fail if the thrift file contains invalid types" in pending

    "honor the structNameToObjectAndTableName parameter" in pending

    "not fail if the thrift file is empty" in pending
  }
}
