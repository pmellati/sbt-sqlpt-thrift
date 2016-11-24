package sqlpt.thriftplugin

import com.twitter.scrooge.frontend.{NullImporter, ThriftParser}
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher
import treehugger.forest._, definitions._, treehuggerDSL._
import scala.util.parsing.input.{Reader, CharArrayReader}
import com.twitter.scrooge.{ast => thrift}
import java.util.StringTokenizer

class ThriftStructToColumnsCaseClassSpec extends Specification {
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
    thriftParser.struct(stringToReader(s)).get

  private def stringToReader(str: String): Reader[Char] =
    new CharArrayReader(str.toCharArray)

  private val thriftParser = new ThriftParser(NullImporter, true)

  private def beSameScalaCodeAs(codeStr: String): Matcher[Tree] = {
    def tokenized(sourceCode: String): List[String] = {
      val Delimiters = " \n()[]{},;:"

      def toList(t: StringTokenizer): List[String] =
        if(t.hasMoreTokens)
          t.nextToken :: toList(t)
        else
          Nil

      toList(new StringTokenizer(sourceCode, Delimiters, true)).filterNot(_.trim.isEmpty)
    }

    (
      (tree: Tree) =>
        tokenized(codeStr) == tokenized(treeToString(tree)),

      (tree: Tree) =>
        s"${treeToString(tree)}\n\nis not the same scala code as:\n$codeStr"
    )
  }
}
