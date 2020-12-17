package com.well.server.utils

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.well.server.Database
import com.well.server.routing.Call
import com.well.serverModels.UserId
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.DriverDataSource
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import kotlin.coroutines.CoroutineContext

class Dependencies(app: Application) {
    val environment = app.environment
    val database = initialiseDatabase(app)
    val jwtConfig = JwtConfig(environment.config)
    private val webSocketPlug = object: WebSocketSession {
        override val coroutineContext: CoroutineContext
            get() = TODO("Not yet implemented")
        override val incoming: ReceiveChannel<Frame>
            get() = TODO("Not yet implemented")
        override var masking: Boolean
            get() = TODO("Not yet implemented")
            set(value) {}
        override var maxFrameSize: Long
            get() = TODO("Not yet implemented")
            set(value) {}

        private val _outgoing = Channel<Frame>()

        init {
            GlobalScope.launch {
                for (frame in _outgoing) {
                    println("plug $frame")
                }
            }
        }

        override val outgoing: SendChannel<Frame>
            get() = _outgoing

        override suspend fun flush() {
            TODO("Not yet implemented")
        }

        override fun terminate() {
            TODO("Not yet implemented")
        }

    }
    val connectedUserSessions = Collections.synchronizedMap(mutableMapOf<UserId, WebSocketSession>(
        0 to webSocketPlug,
        1 to webSocketPlug,
        2 to webSocketPlug,
        3 to webSocketPlug,
    ))
    val calls = Collections.synchronizedList(mutableListOf<Call>())

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        HttpResponseValidator {
            validateResponse { response ->
                val status = response.status.value
                if (status < 300) return@validateResponse

                val responseString = String(response.readBytes())
                val exceptionResponse = Json.parseToJsonElement(responseString)

                when (status) {
                    in 300..399 -> throw Throwable(exceptionResponse.toString())
                    in 400..499 -> throw Throwable(exceptionResponse.toString())
                    in 500..599 -> throw Throwable(exceptionResponse.toString())
                    else -> throw Throwable(exceptionResponse.toString())
                }
            }
        }
    }
}

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
        "com.zaxxer.hikari.pool.PoolBase::class.java",
    ).forEach {
        (when (it) {
            is String -> LoggerFactory.getLogger(it)
            is Class<out Any> -> LoggerFactory.getLogger(it)
            else -> throw IllegalStateException()
        } as ch.qos.logback.classic.Logger)
            .level = ch.qos.logback.classic.Level.WARN
    }

    val dataSource = HikariDataSource(datasourceConfig)
    val driver = dataSource.asJdbcDriver()
    Database.Schema.create(driver)
    val db = Database(driver)
    app.environment.monitor.subscribe(ApplicationStopped) { driver.close() }

    return db
}