package com.well.modules.utils.dbUtils

import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.db.use

private class SqlDriverTransacter(driver: SqlDriver) : TransacterImpl(driver)

fun SqlDriver.migrateIfNeededMySql(schema: SqlDriver.Schema) {
    val driver = this
    val sqlDriverTransacter = SqlDriverTransacter(this)
    val result = sqlDriverTransacter.transactionWithResult<Pair<Boolean, Int>> {
        var needsMetaTable = false
        val version = try {
            executeQuery(
                null,
                "SELECT value FROM __sqldelight__ WHERE name = 'schema_version'",
                0
            ).use {
                (if (it.next()) it.getLong(0)?.toInt() else 0) ?: 0
            }
        } catch (e: Exception) {
            needsMetaTable = true
            0
        }
        needsMetaTable to version
    }
    val (needsMetaTable, version) = result
    if (version >= schema.version) {
        return
    }
    println("migration $version -> ${schema.version}")
    sqlDriverTransacter.transaction {
        if (version == 0) {
            schema.create(driver)
        } else {
            schema.migrate(
                driver,
                version,
                schema.version
            )
        }
        if (needsMetaTable) {
            execute(
                null,
                "CREATE TABLE __sqldelight__(name VARCHAR(64) NOT NULL PRIMARY KEY, value VARCHAR(64))",
                0
            )
        }
        if (version == 0) {
            execute(
                null,
                "INSERT INTO __sqldelight__(name, value) VALUES('schema_version', ${schema.version})",
                0
            )
        } else {
            execute(
                null,
                "UPDATE __sqldelight__ SET value='${schema.version}' WHERE name='schema_version'",
                0
            )
        }
    }
}