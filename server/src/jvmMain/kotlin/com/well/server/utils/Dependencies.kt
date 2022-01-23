package com.well.server.utils

import com.well.modules.db.server.Database
import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.db.server.toChatMessage
import com.well.modules.db.server.toMeeting
import com.well.modules.db.server.toUser
import com.well.modules.models.DeviceId
import com.well.modules.models.Meeting
import com.well.modules.models.Notification
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.flowUtils.MutableMapFlow
import com.well.modules.utils.flowUtils.MutableSetStateFlow
import com.well.modules.utils.ktorUtils.createBaseHttpClient
import com.well.server.routing.UserSession
import com.well.server.utils.notifications.sendNotification
import ch.qos.logback.core.util.Loader
import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.squareup.sqldelight.db.SqlDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.DriverDataSource
import io.ktor.application.*
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.stream.Stream


class Dependencies(app: Application) {
    val environment = app.environment
    val database: Database
    val dbDriver: SqlDriver
    val devApnsClient by lazy {
        createApnsClient(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
    }
    val prodApnsClient by lazy {
        createApnsClient(ApnsClientBuilder.PRODUCTION_APNS_HOST)
    }

    private fun createApnsClient(apnsServer: String) =
        ApnsClientBuilder()
            .setApnsServer(
                apnsServer
            )
            .setSigningKey(
                ApnsSigningKey.loadFromInputStream(
                    resourceStream("services/apns_key.p8"),
                    environment.configProperty("social.apple.teamId"),
                    environment.configProperty("social.apple.apnsKeyId"),
                )
            )
            .build()!!

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private fun resourceStream(name: String): InputStream =
        javaClass.getResourceAsStream("/$name")!!

    init {
        listOf(
            HikariDataSource::class.java,
            com.zaxxer.hikari.pool.HikariPool::class.java,
            com.zaxxer.hikari.pool.HikariPool::class.java.superclass,
            DriverDataSource::class.java,
            HikariConfig::class.java,
            "c.e.pushy.apns.ApnsClientHandler::class.java",
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

        val dbInfo = initialiseDatabase(app)
        database = dbInfo.first
        dbDriver = dbInfo.second

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(resourceStream("services/fcm_service_account.json")))
            .build()

        FirebaseApp.initializeApp(options)
    }

    val jwtConfig = JwtConfig(
        environment.configProperty("jwt.accessTokenSecret"),
    )
    val awsManager: AwsManager by lazy {
        AwsManager(
            environment.configProperty("aws.accessKeyId"),
            environment.configProperty("aws.secretAccessKey"),
            environment.configProperty("aws.bucketName"),
        )
    }
    val connectedUserSessionsFlow = MutableMapFlow<ClientKey, UserSession>()
    val callInfos: MutableList<CallInfo> = Collections.synchronizedList(mutableListOf<CallInfo>())
    val client: HttpClient = createBaseHttpClient()

    fun awsProfileImagePath(
        uid: User.Id,
        ext: String,
    ) = "profilePictures/$uid-${UUID.randomUUID()}.$ext"

    fun getCurrentUser(id: User.Id) = getUser(uid = id, currentUid = id)

    fun getUser(
        uid: User.Id,
        currentUid: User.Id,
    ) = database
        .usersQueries
        .getById(uid)
        .executeAsOne()
        .toUser(
            currentUid = currentUid,
            database = database,
        )

    val messagesToDeliver = MutableSetStateFlow<Pair<DeviceId, ChatMessage.Id>>()
    fun deliverMessageNotificationIfNeeded(id: ChatMessage.Id) {
        coroutineScope.launch {
            val message = database.chatMessagesQueries
                .getById(id)
                .executeAsOne()
                .toChatMessage(database)
            val tokens = database.notificationTokensQueries
                .selectTokenByUid(message.peerId)
                .executeAsList()
            val clientsToDeliver = tokens.map {
                it to ClientKey(it.deviceId, message.peerId)
            }
            println("clientsToDeliver $clientsToDeliver")
            val notification = lazy {
                val senderName = database.usersQueries.getById(message.fromId).executeAsOne().fullName
                val chatUnreadCount = database.chatMessagesQueries.unreadCount(
                    fromId = message.fromId,
                    peerId = message.peerId,
                ).executeAsOne().toInt()

                Notification.ChatMessage(
                    message = message,
                    senderName = senderName,
                    chatUnreadCount = chatUnreadCount,
                    totalUnreadCount = totalUnreadCount(message.peerId),
                )
            }
            clientsToDeliver.forEach {
                deliver(
                    id = id,
                    clientKey = it.second,
                    tokenInfo = it.first,
                    notification = notification,
                )
            }
        }
    }

    private suspend fun CoroutineScope.deliver(
        id: ChatMessage.Id,
        clientKey: ClientKey,
        tokenInfo: SelectTokenByUid,
        notification: Lazy<Notification>,
    ) {
        if (connectedUserSessionsFlow.contains(clientKey)) {
            println("deliver token: waiting socket to deliver")
            val messageToDeliver = clientKey.deviceId to id
            messagesToDeliver.add(messageToDeliver)
            val checkingJob = launch checkingJob@{
                val collectingJob = launch {
                    messagesToDeliver.collect {
                        if (!it.contains(messageToDeliver)) {
                            this@checkingJob.cancel()
                        }
                    }
                }
                delay(10.seconds)
                collectingJob.cancel()
            }
            checkingJob.join()
            if (checkingJob.isCancelled) {
                println("deliver token: socket delivered")
                return
            }
            messagesToDeliver.remove(messageToDeliver)
            println("deliver token: socket not delivered - delivering")
        } else {
            println("deliver token: client not connected - delivering")
        }
        sendNotification(
            notification = notification.value,
            tokenInfo = tokenInfo,
            dependencies = this@Dependencies
        )
    }

    val meetingsToDeliver = MutableSetStateFlow<Pair<DeviceId, Meeting.Id>>()
    fun deliverMeetingNotificationIfNeeded(id: Meeting.Id) {
        coroutineScope.launch {
            val meeting = database.meetingsQueries
                .getById(id)
                .executeAsOne()
                .toMeeting()
            val tokens = database.notificationTokensQueries
                .selectTokenByUid(meeting.expertUid)
                .executeAsList()
            val clientsToDeliver = tokens.map {
                it to ClientKey(it.deviceId, meeting.expertUid)
            }
            val notification = lazy {
                val senderName = database.usersQueries.getById(meeting.creatorUid).executeAsOne().fullName
                Notification.Meeting(
                    meetingId = id,
                    senderName = senderName,
                    chatUnreadCount = 0,
                    totalUnreadCount = totalUnreadCount(meeting.expertUid),
                )
            }
            clientsToDeliver.forEach {
                deliver(
                    id = id,
                    clientKey = it.second,
                    tokenInfo = it.first,
                    notification = notification,
                )
            }
        }
    }

    private suspend fun CoroutineScope.deliver(
        id: Meeting.Id,
        clientKey: ClientKey,
        tokenInfo: SelectTokenByUid,
        notification: Lazy<Notification>,
    ) {
        if (connectedUserSessionsFlow.contains(clientKey)) {
            println("deliver token: waiting socket to deliver")
            val meetingToDeliver = clientKey.deviceId to id
            meetingsToDeliver.add(meetingToDeliver)
            val checkingJob = launch checkingJob@{
                val collectingJob = launch {
                    meetingsToDeliver.collect {
                        if (!it.contains(meetingToDeliver)) {
                            this@checkingJob.cancel()
                        }
                    }
                }
                delay(10.seconds)
                collectingJob.cancel()
            }
            checkingJob.join()
            if (checkingJob.isCancelled) {
                println("deliver token: socket delivered")
                return
            }
            meetingsToDeliver.remove(meetingToDeliver)
            println("deliver token: socket not delivered - delivering")
        } else {
            println("deliver token: client not connected - delivering")
        }
        sendNotification(
            notification = notification.value,
            tokenInfo = tokenInfo,
            dependencies = this@Dependencies
        )
    }

    private fun totalUnreadCount(uid: User.Id): Int {
        val maybeUnreadChat = database.lastReadMessagesQueries.selectByPeerId(uid)
            .executeAsList()
        val unreadChatCount = maybeUnreadChat.sumOf { lastReadMessage ->
            database.chatMessagesQueries.unreadCount(
                fromId = lastReadMessage.fromId,
                peerId = lastReadMessage.peerId,
            ).executeAsOne().toInt()
        }
        val unreadMeetingsCount = database.meetingsQueries
            .countByState(
                expertUid = uid,
                state = Meeting.State.Requested,
            )
            .executeAsOne().toInt()
        return unreadMeetingsCount + unreadChatCount
    }
}