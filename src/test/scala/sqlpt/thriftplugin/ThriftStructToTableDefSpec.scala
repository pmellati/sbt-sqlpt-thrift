package sqlpt.thriftplugin

import org.specs2.mutable.Specification
import treehugger.forest._, definitions._, treehuggerDSL._
import com.twitter.scrooge.{ast => thrift}
import testhelpers.Helpers
import ThriftFileToTableDefs.thriftStructToTableDef

class ThriftStructToTableDefSpec extends Specification with Helpers {
  "ThriftFileToTableDefs.thriftStructToTableDef(...)" should {
    "successfully translate a valid thrift struct into a SQLpt TableDef" in {
      val struct = parseStruct(
        s"""
           |struct Car {
           |     1: required string car_id;
           |     2: required byte model;
           |}
        """.stripMargin)

      translate(struct, "CarsObjectName", "my_db.cars_table").get must beSameScalaCodeAs(
        """
          |object CarsObjectName extends TableDef {
          |  override def name = "my_db.cars_table"
          |
          |  case class Columns(
          |    car_id: Column[Str] = "car_id",
          |    model:  Column[Num] = "model"
          |  )
          |
          |  override def cols = Columns()
          |}
        """.stripMargin)
    }

    "fail to translate an invalid thrift struct" in {
      val struct = parseStruct(
        s"""
           |struct Car {
           |     1: required string car_id;
           |     2: required UnsupportedType model;
           |}
        """.stripMargin)

      translate(struct, "CarsObjectName", "my_db.cars_table") must beFailedTry
    }
  }

  private val translate = thriftStructToTableDef(ThriftStructToColumnsCaseClass(identity, identity)) _

  private def parseStruct(s: String): thrift.Struct =
    thriftParser.struct(toReader(s)).get
}
