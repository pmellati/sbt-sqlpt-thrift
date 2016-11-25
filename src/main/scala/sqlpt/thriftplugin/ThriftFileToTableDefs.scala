package sqlpt.thriftplugin

import com.twitter.scrooge.{ast => thrift}
import treehugger.forest.{ModuleDef => ObjectDef, _}, definitions._, treehuggerDSL._
import scala.util.{Try, Failure, Success}
import ThriftFileToTableDefs._

case class ThriftFileToTableDefs(structToCaseClass: ThriftStructToColumnsCaseClass)
extends (thrift.Document => List[ObjectDef]) {
  def apply(doc: thrift.Document): List[ObjectDef] = {
    ???
  }
}

object ThriftFileToTableDefs {
  def thriftStructToTableDef(
    structToCaseClass: ThriftStructToColumnsCaseClass)(
    struct: thrift.Struct,
    objectName: String,
    fullTableName: String
  ): Try[ObjectDef] =
    structToCaseClass(struct) map {Columns_CaseClassDef =>
      OBJECTDEF(objectName) withParents "TableDef" := Block(
        DEF("name") withFlags Flags.OVERRIDE := LIT(fullTableName),

        Columns_CaseClassDef,

        DEF("cols") withFlags Flags.OVERRIDE := Apply(Ident("Columns"), List())
      )
    }
}