package sqlpt.thriftplugin

import sbt._
import Keys._
import treehugger.forest._
import sqlpt.thriftplugin.Utils.BasicThriftParser

object SqlptThriftPlugin extends AutoPlugin with BasicThriftParser {
  object autoImport {
    object sqlpt {
      type TableInfo = ThriftFileToCompilationUnit.TableInfo
      val  TableInfo = ThriftFileToCompilationUnit.TableInfo

      lazy val thriftDir = settingKey[File]("""
        |The thrift structs in the .thrift files in this directory will be considered for being turned into table
        |definitions.
      """.stripMargin)

      lazy val tableInfos = settingKey[PartialFunction[String, TableInfo]]("""
        |Partial function from thrift struct names to table info. Table definitions will only be generated for structs
        |that are in the domain of this partial function.
      """.stripMargin)

      lazy val outputDir = settingKey[File]("""
        |The directory in which SQLpt table definition files will be written.
      """.stripMargin)

      lazy val thriftFieldNameToCaseClassFieldName = settingKey[String => String]("""
        |Function for translating thrift field names into TableDef case class field names.
      """.stripMargin)

      lazy val thriftFieldNameToTableColumnName = settingKey[String => String]("""
        |Function for translating thrift field names into database column names.
      """.stripMargin)

      lazy val genTableDefs = taskKey[Seq[File]]("""
        |Generate SQLpt TableDefs from thrift files.
      """.stripMargin)
    }
  }

  import autoImport.sqlpt._

  override val projectSettings = Seq(
    outputDir := sourceManaged.value / "main" / "thrift" / "sqlpt",

    thriftFieldNameToCaseClassFieldName := identity,

    thriftFieldNameToTableColumnName := identity,

    genTableDefs := {
      if (!thriftDir.value.isDirectory)
        sys.error("The sqlpt.thriftDir sbt setting should be a directory.")
      else {
        val thriftFiles = thriftDir.value.listFiles.filter(f => f.isFile && f.name.endsWith(".thrift")).toList

        thriftFiles.map {thriftFile =>
          val thriftFileContents = IO.read(thriftFile)
          val thriftDoc = thriftParser.document(toReader(thriftFileContents)).getOrElse {
            sys.error(s"Failed to parse '${thriftFile.getAbsolutePath}' as a valid thrift file.")
          }

          val thriftDocToCompilationUnit = ThriftFileToCompilationUnit(
            ThriftStructToColumnsCaseClass(
              thriftFieldNameToCaseClassFieldName.value,
              thriftFieldNameToTableColumnName.value),
            tableInfos.value)

          val outputFileContent = treeToString(thriftDocToCompilationUnit(thriftDoc).get)

          val outputFileName = thriftFile.name.dropRight(".thrift".length) ++ ".scala"

          val outputFile = outputDir.value / outputFileName

          IO.write(outputFile, outputFileContent)

          outputFile
        }
      }
    },

    sourceGenerators in Compile += genTableDefs.taskValue
  )
}
