package com.well.server.utils

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.well.modules.db.helper.SetEnumColumnAdapter
import com.well.modules.db.helper.migrateIfNeeded
import com.well.server.Database
import com.well.server.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.DriverDataSource
import io.ktor.application.*
import org.slf4j.LoggerFactory
import java.io.File

fun initialiseDatabase(app: Application): Database {
    val dbConfig = app.environment.config.config("database")
    var connectionUrl: String = dbConfig.property("connection")
        .getString()
    val userName = dbConfig.property("username").getString()
    val pass = dbConfig.property("password").getString()

    val rdsConnection = dbConfig.property("rdsConnection").getString()
    val significantPart = rdsConnection.substringAfter("//")

    val datasourceConfig = HikariConfig().apply {
        // Ensure that rdsConnection is not empty
        if (significantPart.split(":", "/").all { it.isNotEmpty() }) {
            jdbcUrl = "$rdsConnection?user=$userName&password=$pass"
            driverClassName = "com.mysql.cj.jdbc.Driver"
        } else {
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

    listOf(
        HikariDataSource::class.java,
        com.zaxxer.hikari.pool.HikariPool::class.java,
        DriverDataSource::class.java,
        HikariConfig::class.java,
        "com.zaxxer.hikari.pool.PoolBase::class.java",
    ).forEach {
        (when (it) {
            is String -> LoggerFactory.getLogger(it)
            is Class<out Any> -> LoggerFactory.getLogger(it)
            else -> throw IllegalStateException()
        } as ch.qos.logback.classic.Logger)
            .apply {
                level = ch.qos.logback.classic.Level.WARN
            }
    }

    val dataSource = HikariDataSource(datasourceConfig)
    val driver = dataSource.asJdbcDriver()
    driver.migrateIfNeeded(Database.Schema)
    val db = Database(
        driver = driver,
        UsersAdapter = Users.Adapter(
            typeAdapter = EnumColumnAdapter(),
            credentialsAdapter = EnumColumnAdapter(),
            academicRankAdapter = EnumColumnAdapter(),
            languagesAdapter = SetEnumColumnAdapter(),
            skillsAdapter = SetEnumColumnAdapter(),
        )
    )
    app.environment.monitor.subscribe(ApplicationStopped) { driver.close() }
    db.usersQueries
        .selectUninitialized()
        .executeAsList()
        .let { uninitialized ->
            if (uninitialized.isNotEmpty()) {
                println("cleaning uninitialized: $uninitialized")
                db.usersQueries.deleteUninitialized()
            }
        }
    return db
}