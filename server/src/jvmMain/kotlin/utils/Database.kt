package com.well.server.utils

import com.well.modules.db.server.Database
import com.well.modules.db.server.Users
import com.well.modules.db.server.invoke
import com.well.modules.utils.dbUtils.migrateIfNeededMySql
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import java.io.File

fun initialiseDatabase(app: Application): Pair<Database, SqlDriver> {
    val dbConfig = app.environment.config.config("database")
    val userName = dbConfig.property("username").getString()
    val pass = dbConfig.property("password").getString()

    val datasourceConfig = HikariConfig().apply {
        try {
            val rdsHostname = dbConfig.property("rdsHostname").getString()
            val overrideRdsHostname = dbConfig.propertyOrNull("overrideRdsHostname")?.getString()
            val rdsPort = dbConfig.property("rdsPort").getString()
            val rdsDbName = dbConfig.property("rdsDbName").getString()
            jdbcUrl = "jdbc:mysql://${overrideRdsHostname ?: rdsHostname}:$rdsPort/$rdsDbName" +
                    "?user=$userName&password=$pass"
            driverClassName = "com.mysql.cj.jdbc.Driver"
        } catch (e: Exception) {
            // drs info missing - load local file
            var connectionUrl: String = dbConfig.property("connection")
                .getString()
            // If this is a local h2 database, ensure the directories exist
            if (connectionUrl.startsWith("jdbc:h2:file:")) {
                val dbFile = File(connectionUrl.removePrefix("jdbc:h2:file:")).absoluteFile
                if (!dbFile.parentFile.exists()) {
                    dbFile.parentFile.mkdirs()
                }
                connectionUrl = "jdbc:h2:file:${dbFile.absolutePath}"
            }
            jdbcUrl = connectionUrl
            username = userName
            password = pass
        }
        maximumPoolSize = dbConfig.propertyOrNull("poolSize")
            ?.getString()
            ?.toInt() ?: 10
    }

    val dataSource = HikariDataSource(datasourceConfig)
    val driver = dataSource.asJdbcDriver()
    driver.migrateIfNeededMySql(Database.Schema)
    val db = Database(driver = driver)
    app.environment.monitor.subscribe(ApplicationStopped) { driver.close() }
    db.usersQueries
        .selectUninitialized()
        .executeAsList()
        .let { uninitialized: List<Users> ->
            if (uninitialized.isNotEmpty()) {
                println("cleaning uninitialized: $uninitialized")
                db.usersQueries.deleteUninitialized()
            }
        }
    return db to driver
}

fun SqlDriver.executeQueryAndPrettify(sql: String): String =
    executeQuery(
        null,
        sql,
        0
    ).run {
        var result = ""
        while (next()) {
            try {
                var i = 0
                while (true) {
                    result += getString(i++) + " "
                }
            } catch (_: Exception) {
            }
            result += "\n"
        }
        result
    }