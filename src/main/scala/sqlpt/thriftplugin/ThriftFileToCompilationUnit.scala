package sqlpt.thriftplugin

import com.twitter.scrooge.{ast => thrift}
import treehugger.forest.{ModuleDef => ObjectDef, PackageDef => CompilationUnit, _}, treehuggerDSL._
import scala.util.Try
import Utils.SequenceForTries
import ThriftFileToCompilationUnit._

case class ThriftFileToCompilationUnit(
  structToCaseClass:              ThriftStructToColumnsCaseClass,
  structNameToObjectAndTableName: PartialFunction[StructName, ObjectAndTableName]
) extends (thrift.Document => Try[CompilationUnit]) {
  def apply(doc: thrift.Document): Try[CompilationUnit] = {
    val tableDefs = doc.defs.collect {
      case struct: thrift.Struct if structNameToObjectAndTableName.isDefinedAt(struct.originalName) =>
        val names = structNameToObjectAndTableName(struct.originalName)
        thriftStructToTableDef(structToCaseClass)(struct, names.objectName, names.tableName)
    }.sequence

    tableDefs map {tableDefs =>
      val block = BLOCK(IMPORT("sqlpt.api._") :: tableDefs: _*)

      doc.namespace("scala") match {
        case Some(packageName) =>
          block inPackage packageName.fullName
        case None =>
          block.withoutPackage
      }
    }
  }
}

object ThriftFileToCompilationUnit {
  def thriftStructToTableDef(
    structToCaseClass: ThriftStructToColumnsCaseClass)(
    struct:            thrift.Struct,
    objectName:        String,
    fullTableName:     String
  ): Try[ObjectDef] =
    structToCaseClass(struct) map {Columns_CaseClassDef =>
      OBJECTDEF(objectName) withParents "TableDef" := Block(
        DEF("name") withFlags Flags.OVERRIDE := LIT(fullTableName),

        Columns_CaseClassDef,

        DEF("cols") withFlags Flags.OVERRIDE := Apply(Ident("Columns"), List())
      )
    }

  type StructName = String

  case class ObjectAndTableName(objectName: String, tableName: String)
}