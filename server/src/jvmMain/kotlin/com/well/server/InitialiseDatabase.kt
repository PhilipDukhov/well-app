package com.well.server

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.DriverDataSource
import io.ktor.application.*
import org.slf4j.LoggerFactory
import java.io.File

fun initialiseDatabase(app: Application): Database {
    val dbConfig = app.environment.config.config("database")
    var connectionUrl = dbConfig.property("connection").getString()

    // If this is a local h2 database, ensure the directories exist
    if (connectionUrl.startsWith("jdbc:h2:file:")) {
        val dbFile = File(connectionUrl.removePrefix("jdbc:h2:file:")).absoluteFile
        if (!dbFile.parentFile.exists()) {
            dbFile.parentFile.mkdirs()
        }
        connectionUrl = "jdbc:h2:file:${dbFile.absolutePath}"
    }

    val datasourceConfig = HikariConfig().apply {
        jdbcUrl = connectionUrl
        username = dbConfig.propertyOrNull("username")?.getString()
        password = dbConfig.propertyOrNull("password")?.getString()
        maximumPoolSize = dbConfig.propertyOrNull("poolSize")?.getString()?.toInt() ?: 10
    }
    listOf(
        HikariDataSource::class.java,
        com.zaxxer.hikari.pool.HikariPool::class.java,
        DriverDataSource::class.java,
        HikariConfig::class.java,
    ).forEach {
        (LoggerFactory.getLogger(it) as ch.qos.logback.classic.Logger)
            .level = ch.qos.logback.classic.Level.WARN
    }

    val dataSource = HikariDataSource(datasourceConfig)
    val driver = dataSource.asJdbcDriver()
    Database.Schema.create(driver)
    val db = Database(driver)
    app.environment.monitor.subscribe(ApplicationStopped) { driver.close() }

    return db
}