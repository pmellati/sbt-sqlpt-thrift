package sqlpt.thriftplugin

import org.specs2.mutable.Specification
import treehugger.forest._
import com.twitter.scrooge.{ast => thrift}
import testhelpers.Helpers

class ThriftStructToColumnsCaseClassSpec extends Specification with Helpers {
  "ThriftStructToColumnsCaseClass" should {
    "successfully translate a thrift struct into a case class" in {
      val toCaseClass = ThriftStructToColumnsCaseClass(identity, identity)

      val struct = parseStruct(
        """
          |struct Car {
          |     1: required string car_id;           // Some comment.
          |     2: required string model;
          |        required i32    price = "PT";     // Default value gets ignored.
          |     4: optional bool   preowned;
          |}
        """.stripMargin)

      toCaseClass(struct).get must beSameScalaCodeAs(
        """
          |case class Columns(
          |  car_id: Column[Str] = "car_id",
          |  model: Column[Str] = "model",
          |  price: Column[Num] = "price",
          |  preowned: Column[Nullable[Bool]] = "preowned"
          |)
        """.stripMargin)
    }

    "utilize the thriftFieldNameToCaseClassFieldName transformation" in {
      val struct = parseStruct(
        """
          |struct Car {
          |     1: required string car_id;
          |     2: required string model;
          |}
        """.stripMargin)

      def toCaseClassFieldName(thriftFieldName: String): String =
        s"transformed_$thriftFieldName"

      val toCaseClass = ThriftStructToColumnsCaseClass(
        thriftFieldNameToCaseClassFieldName = toCaseClassFieldName,
        identity)

      toCaseClass(struct).get must beSameScalaCodeAs(
        """
          |case class Columns(
          |  transformed_car_id: Column[Str] = "car_id",
          |  transformed_model:  Column[Str] = "model"
          |)
        """.stripMargin)
    }

    "utilize the thriftFieldNameToTableFieldName transformation" in {
      val struct = parseStruct(
        """
          |struct Car {
          |     1: required string car_id;
          |     2: required string model;
          |}
        """.stripMargin)

      def toTableFieldName(thriftFieldName: String): String =
        s"transformed_$thriftFieldName"

      val toCaseClass = ThriftStructToColumnsCaseClass(
        identity,
        thriftFieldNameToTableFieldName = toTableFieldName)

      toCaseClass(struct).get must beSameScalaCodeAs(
        """
          |case class Columns(
          |  car_id: Column[Str] = "transformed_car_id",
          |  model:  Column[Str] = "transformed_model"
          |)
        """.stripMargin)
    }

    "fail if the thrift struct contains a field with an unsupported type" in {
      val structWithBadType = parseStruct(
        s"""
          |struct Car {
          |     1: required string car_id;
          |     2: required InvalidTypeName model;
          |}
        """.stripMargin)

      val structWithValidTypes = parseStruct(
        s"""
           |struct Car {
           |     1: required string car_id;
           |     2: required byte model;
           |}
        """.stripMargin)

      val toCaseClass = ThriftStructToColumnsCaseClass(identity, identity)

      toCaseClass(structWithBadType) must
        beFailedTry[Tree].withThrowable[RuntimeException](".* is not supported as a SQLpt column type.*")

      toCaseClass(structWithValidTypes) must beSuccessfulTry
    }
  }

  private def parseStruct(s: String): thrift.Struct =
    thriftParser.struct(toReader(s)).get
}
