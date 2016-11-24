package sqlpt.thriftplugin

import sbt._
import Keys._

object SqlptThriftPlugin extends AutoPlugin {
  object autoImport {
    object sqlpt {
      case class TableInfo(namespace: String, name: String)

      lazy val thriftDir  = settingKey[File](
        """
          |The thrift structs in the .thrift files in this directory will be considered for being turned into table
          |definitions.
        """.stripMargin)

      lazy val tableInfos = settingKey[PartialFunction[String, TableInfo]](
        """
          |Partial function from thrift struct names to table info.
          |
          |Table definitions will only be generated for structs that are in the domain of this partial function.
        """.stripMargin)
    }
  }

  import autoImport.sqlpt._

  lazy val genTableDefs = taskKey[Seq[File]]("Generate SQLpt TableDefs from thrift files.")

  override val projectSettings = Seq(
    genTableDefs := {
      val outputDir = sourceManaged.value / "main" / "thrift" / "sqlpt"

      val outputFile = outputDir / "theTableDef.scala"

      IO.write(outputFile,
        """
          |object Wuzuuuuup {}
        """.stripMargin)

      Seq(outputFile)
    },

    sourceGenerators in Compile += genTableDefs.taskValue
  )
}
