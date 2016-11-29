[![Build Status](https://travis-ci.org/pmellati/sbt-sqlpt-thrift.svg?branch=master)](https://travis-ci.org/pmellati/sbt-sqlpt-thrift)

# sbt-sqlpt-thrift

Sbt plugin to generate [SQLpt](https://github.com/pmellati/SQLpt) table definitions from thrift files.

## Getting started

Add the following line to your `project/plugins.sbt` file:

```scala
addSbtPlugin("sqlpt" % "sbt-sqlpt-thrift" % "0.1")
```

Next, you'll need to manually add the settings of the plugin to your project(s) (for some reason, I couldn't get these to get automatically added). For instance:

```scala
lazy val myProject = (project in file("."))
  .settings(SqlptThriftPlugin.projectSettings)    // The line you need to add.
  .settings(
    ...
  )
```

Finally, you must define, at the very minimum, the following two settings for your project(s). Example definitions:

```scala
sqlpt.thriftDir := sourceDirectory {_ / "main" / "thrift" / "sqlpt"}.value

sqlpt.tableInfos := {case structName => sqlpt.TableInfo(structName, structName)}
```

See the [settings](#settings) section for an overview of the supported keys and their meanings.

## Settings

* `sqlpt.thriftDir: SettingKey[File]`: Where to look for thrift definitions. The thrift structs in the .thrift files in this directory will be considered for being turned into table definitions.

  You must provide a value for this setting.

* `sqlpt.tableInfos: SettingKey[PartialFunction[String, sqlpt.TableInfo]]`: Partial function from thrift struct names to table info. Table definitions will only be generated for structs that are in the domain of this partial function.

  Where `sqlpt.TableInfo` is defined as follows:
  ```scala
  case class TableInfo(objectName: String, tableName: String)
  ```

  To paraphrase, this key serves two purposes:
  * If you have a standard for translating from thrift struct names into table names (and in-code object names), you can code it up here.
  * You are free to omit certain structs from translation.

  You must provide a value for this setting.

* `sqlpt.outputDir: SettingKey[File]`: The directory in which SQLpt table definition files will be written.

  Default value for most people will be `target/src_managed/main/thrift/sqlpt`.

* `sqlpt.thriftFieldNameToCaseClassFieldName: SettingKey[String => String]`: Function for translating thrift field names into TableDef case class field names.

  Default value is `identity`.

* `sqlpt.thriftFieldNameToTableColumnName: SettingKey[String => String]`: Function for translating thrift field names into database column names.

  Default value is `identity`.
