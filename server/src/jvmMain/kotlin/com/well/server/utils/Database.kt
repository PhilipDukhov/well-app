package com.well.server.utils

import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.TransacterImpl
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
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
    return db
}

private class SqlDriverTransacter(driver: SqlDriver) : TransacterImpl(driver)

fun SqlDriver.migrateIfNeeded(schema: SqlDriver.Schema) {
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
        if (version == 0) schema.create(this@migrateIfNeeded) else schema.migrate(
            this@migrateIfNeeded,
            version,
            schema.version
        )
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

@Suppress("FunctionName")
private inline fun <reified T : Enum<T>> SetEnumColumnAdapter() =
    SetEnumColumnAdapter(enumValues<T>())

private class SetEnumColumnAdapter<T : Enum<T>>(
    private val enumValues: Array<out T>
) : ColumnAdapter<Set<T>, String> {
    override fun decode(databaseValue: String): Set<T> =
        if (databaseValue.isNotEmpty())
            databaseValue
                .split(",")
                .mapTo(HashSet()) { value -> enumValues.first { it.name == value } }
        else setOf()

    override fun encode(value: Set<T>) =
        value.joinToString(separator = ",") { it.name }
}