package sqlpt.thriftplugin

import com.twitter.scrooge.{ast => thrift}
import treehugger.forest._, definitions._, treehuggerDSL._
import scala.util.{Try, Failure, Success}
import Utils.SequenceForTries
import ThriftStructToColumnsCaseClass._

case class ThriftStructToColumnsCaseClass(
  thriftFieldNameToCaseClassFieldName: String => String,
  thriftFieldNameToTableFieldName:     String => String
) extends (thrift.Struct => Try[ClassDef]) {

  def apply(thrStruct: thrift.Struct): Try[ClassDef] = {
    val caseClassParams = thrStruct.fields.map {field =>
      thriftFieldToColumnType(field.fieldType, field.requiredness) map {colType =>
        PARAM(
          thriftFieldNameToCaseClassFieldName(field.originalName),
          colType
        ) := LIT(thriftFieldNameToTableFieldName(field.originalName))
          : ValDef
      }
    }.sequence

    for {
      caseClassParamss <- caseClassParams
    } yield CASECLASSDEF("Columns") withParams caseClassParamss
  }
}

object ThriftStructToColumnsCaseClass {
  protected[thriftplugin]
  def thriftFieldToColumnType(fieldType: thrift.FieldType, requiredness: thrift.Requiredness): Try[Type] = {
    import thrift._

    (fieldType match {
      case TBool =>
        Success("Bool")
      case TByte | TI16 | TI32 | TI64 | TDouble =>
        Success("Num")
      case TString =>
        Success("Str")
      case other =>
        Failure(new RuntimeException(s"Thrift type $other is not supported as a SQLpt column type."))
    }) map {valueTypeName =>
      val valueType = TYPE_REF(valueTypeName)

      val typeWithNullability =
        if(requiredness.isRequired)
          valueType
        else
          TYPE_REF("Nullable") TYPE_OF valueType

      TYPE_REF("Column") TYPE_OF typeWithNullability
    }
  }
}