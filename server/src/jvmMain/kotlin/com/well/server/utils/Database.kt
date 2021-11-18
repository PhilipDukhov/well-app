package com.well.server.utils

import com.well.modules.db.server.Availabilities
import com.well.modules.db.server.ChatMessages
import com.well.modules.db.server.Database
import com.well.modules.db.server.Favourites
import com.well.modules.db.server.LastReadMessages
import com.well.modules.db.server.Ratings
import com.well.modules.db.server.Users
import com.well.modules.models.Availability
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.dbUtils.SetEnumColumnAdapter
import com.well.modules.utils.dbUtils.migrateIfNeeded
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
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
            idAdapter = User.Id.ColumnAdapter,
        ),
        AvailabilitiesAdapter = Availabilities.Adapter(
            repeatAdapter = EnumColumnAdapter(),
            ownerIdAdapter = User.Id.ColumnAdapter,
            idAdapter = Availability.Id.ColumnAdapter,
        ),
        ChatMessagesAdapter = ChatMessages.Adapter(
            fromIdAdapter = User.Id.ColumnAdapter,
            peerIdAdapter = User.Id.ColumnAdapter,
            idAdapter = ChatMessage.Id.ColumnAdapter,
        ),
        FavouritesAdapter = Favourites.Adapter(
            ownerAdapter = User.Id.ColumnAdapter,
            destinationAdapter = User.Id.ColumnAdapter,
        ),
        LastReadMessagesAdapter = LastReadMessages.Adapter(
            fromIdAdapter = User.Id.ColumnAdapter,
            peerIdAdapter = User.Id.ColumnAdapter,
            messageIdAdapter = ChatMessage.Id.ColumnAdapter,
        ),
        RatingsAdapter = Ratings.Adapter(
            ownerAdapter = User.Id.ColumnAdapter,
            destinationAdapter = User.Id.ColumnAdapter,
        ),
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