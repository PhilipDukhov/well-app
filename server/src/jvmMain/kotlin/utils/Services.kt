package com.well.server.utils

import com.well.modules.db.server.Database
import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.db.server.toChatMessage
import com.well.modules.db.server.toMeeting
import com.well.modules.db.server.toUser
import com.well.modules.models.CallId
import com.well.modules.models.DeviceId
import com.well.modules.models.Meeting
import com.well.modules.models.Notification
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.flowUtils.MutableMapFlow
import com.well.modules.utils.flowUtils.MutableSetStateFlow
import com.well.modules.utils.ktorUtils.createBaseHttpClient
import com.well.server.routing.UserSession
import com.well.server.utils.notifications.sendNotification
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import java.io.InputStream
import java.util.*

class Services(app: Application) {
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
    val ongoingCallInfos: MutableMap<CallId, OngoingCallInfo> = Collections.synchronizedMap(mutableMapOf<CallId, OngoingCallInfo>())
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

    data class PendingNotificationId(val deviceId: DeviceId, val itemId: ItemId) {
        sealed interface ItemId {
            data class Meeting(val meetingId: com.well.modules.models.Meeting.Id) : ItemId
            data class ChatMessage(val chatMessageId: com.well.modules.models.chat.ChatMessage.Id) : ItemId
            data class IncomingCall(val callId: CallId) : ItemId
            data class EndCall(val callId: CallId) : ItemId
        }
    }

    fun deliverMessageNotification(id: ChatMessage.Id) {
        deliverNotificationIfNeeded(
            getItem = {
                database.chatMessagesQueries
                    .getById(id)
                    .executeAsOne()
                    .toChatMessage(database)
            },
            itemId = PendingNotificationId.ItemId.ChatMessage(id),
            getPeerId = ChatMessage::peerId,
            senderId = ChatMessage::fromId,
            buildNotification = { message, senderName, totalUnreadCount ->
                val chatUnreadCount = database.chatMessagesQueries.unreadCount(
                    fromId = message.fromId,
                    peerId = message.peerId,
                ).executeAsOne().toInt()
                Notification.ChatMessage(
                    message = message,
                    senderName = senderName,
                    chatUnreadCount = chatUnreadCount,
                    totalUnreadCount = totalUnreadCount,
                )
            }
        )
    }

    fun deliverMeetingNotification(id: Meeting.Id, senderId: User.Id) {
        deliverNotificationIfNeeded(
            getItem = {
                database.meetingsQueries
                    .getById(id)
                    .executeAsOne()
                    .toMeeting()
            },
            itemId = PendingNotificationId.ItemId.Meeting(id),
            getPeerId = { if (senderId == expertUid) creatorUid else expertUid },
            senderId = { senderId },
            buildNotification = Notification::Meeting,
        )
    }

    fun deliverCallNotification(webSocketMsg: WebSocketMsg.Back.IncomingCall, peerId: User.Id) {
        deliverNotificationIfNeeded(
            getItem = { webSocketMsg },
            itemId = PendingNotificationId.ItemId.IncomingCall(webSocketMsg.callId),
            getPeerId = { peerId },
            senderId = { webSocketMsg.user.id },
            waitToDeliverDelay = 3.seconds,
            buildNotification = { _, _, unreadCount: Int ->
                Notification.Voip.IncomingCall(webSocketMsg, unreadCount)
            }
        )
    }

    fun deliverEndCallNotification(webSocketMsg: WebSocketMsg.Back.IncomingCall, peerId: User.Id) {
        deliverNotificationIfNeeded(
            getItem = { webSocketMsg },
            itemId = PendingNotificationId.ItemId.EndCall(webSocketMsg.callId),
            getPeerId = { peerId },
            senderId = { webSocketMsg.user.id },
            waitToDeliverDelay = 3.seconds,
            buildNotification = { _, _, unreadCount: Int ->
                Notification.Voip.IncomingCall(webSocketMsg, unreadCount)
            }
        )
    }

    val pendingNotificationIds = MutableSetStateFlow<PendingNotificationId>()
    private fun <Item, N : Notification> deliverNotificationIfNeeded(
        getItem: () -> Item,
        itemId: PendingNotificationId.ItemId,
        getPeerId: Item.() -> User.Id,
        senderId: Item.() -> User.Id,
        waitToDeliverDelay: Duration = 10.seconds,
        buildNotification: (Item, senderName: String, unreadCount: Int) -> N,
    ) {
        val item = getItem()
        val peerId = item.getPeerId()
        val tokens = database.notificationTokensQueries
            .selectTokenByUid(peerId)
            .executeAsList()
        val clientsToDeliver = tokens.map {
            it to ClientKey(it.deviceId, peerId)
        }
        val notification = lazy {
            buildNotification(
                item,
                database.usersQueries
                    .getById(item.senderId())
                    .executeAsOne()
                    .fullName,
                totalUnreadCount(peerId)
            )
        }
        clientsToDeliver.forEach {
            coroutineScope.launch {
                deliver(
                    itemId = itemId,
                    clientKey = it.second,
                    tokenInfo = it.first,
                    waitToDeliverDelay = waitToDeliverDelay,
                    notification = notification,
                )
            }
        }
    }

    private suspend fun deliver(
        itemId: PendingNotificationId.ItemId,
        clientKey: ClientKey,
        tokenInfo: SelectTokenByUid,
        waitToDeliverDelay: Duration,
        notification: Lazy<Notification>,
    ) {
        if (connectedUserSessionsFlow.contains(clientKey)) {
            println("deliver token: waiting socket to deliver")
            val messageToDeliver = PendingNotificationId(clientKey.deviceId, itemId)
            pendingNotificationIds.add(messageToDeliver)
            val checkingJob = coroutineScope.launch checkingJob@{
                val collectingJob = coroutineScope.launch {
                    pendingNotificationIds.collect {
                        if (!it.contains(messageToDeliver)) {
                            this@checkingJob.cancel()
                        }
                    }
                }
                delay(waitToDeliverDelay)
                collectingJob.cancel()
            }
            checkingJob.join()
            if (checkingJob.isCancelled) {
                println("deliver token: socket delivered")
                return
            }
            pendingNotificationIds.remove(messageToDeliver)
            println("deliver token: socket not delivered - delivering")
        } else {
            println("deliver token: client not connected - delivering")
        }
        sendNotification(
            notification = notification.value,
            tokenInfo = tokenInfo,
            services = this@Services
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