package sqlpt.thriftplugin

import org.specs2.mutable.Specification
import com.twitter.scrooge.{ast => thrift}, thrift.Requiredness
import treehugger.forest._
import scala.util.{Try, Success}
import testhelpers.Helpers
import ThriftStructToColumnsCaseClass.thriftFieldToColumnType

class ThriftFieldToColumnTypeSpec extends Specification with Helpers {
  "ThriftStructToColumnsCaseClass.thriftFieldToColumnType(fieldType, requiredness)" should {
    "successfully translate supported thrift types into the expected SQLpt column types" in {
      Seq(
        "bool"   -> "Bool",
        "byte"   -> "Num",
        "i16"    -> "Num",
        "i32"    -> "Num",
        "i64"    -> "Num",
        "double" -> "Num",
        "string" -> "Str"
      ) map {case (thriftType, expectedSqlptType) =>
        getColumnTypeString(thriftType, Requiredness.Required) must_==
          Success(s"Column[$expectedSqlptType]")

        getColumnTypeString(thriftType, Requiredness.Default) must_==
          Success(s"Column[Nullable[$expectedSqlptType]]")

        getColumnTypeString(thriftType, Requiredness.Optional) must_==
          Success(s"Column[Nullable[$expectedSqlptType]]")
      }
    }

    "fail to translate unsupported thrift types" in {
      Seq(
        """InvalidTypeName""",
        """i8""",
        """binary""",
        """slist""",
        """map<string, string>""",
        """set<string>""",
        """list<string>"""
      ) map {unsupportedType =>
        getColumnTypeString(unsupportedType, Requiredness.Required) must
          beFailedTry[String].withThrowable[RuntimeException]("Thrift type .+ is not supported as a SQLpt column type.")
      }
    }
  }

  private def getColumnTypeString(thriftFieldTypeName: String, requiredness: Requiredness): Try[String] =
    thriftFieldToColumnType(
      parseThriftFieldType(thriftFieldTypeName),
      requiredness
    ).map(treeToString(_))

  private def parseThriftFieldType(s: String): thrift.FieldType =
    thriftParser.fieldType(toReader(s)).get
}
