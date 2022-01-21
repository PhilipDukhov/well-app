package com.well.server.utils

import com.well.modules.db.server.Database
import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.db.server.toChatMessage
import com.well.modules.db.server.toUser
import com.well.modules.models.DeviceId
import com.well.modules.models.Notification
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.flowUtils.MutableMapFlow
import com.well.modules.utils.flowUtils.MutableSetStateFlow
import com.well.modules.utils.ktorUtils.createBaseHttpClient
import com.well.server.routing.UserSession
import com.well.server.utils.notifications.sendNotification
import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.squareup.sqldelight.db.SqlDriver
import io.ktor.application.*
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import java.io.File
import java.io.FileInputStream
import java.util.*


class Dependencies(app: Application) {
    val environment = app.environment
    val database: Database
    val dbDriver: SqlDriver
    val apnsClient: ApnsClient

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private fun resourceFile(name: String): File =
        File(javaClass.getResource("/$name")!!.file)

    init {
        val dbInfo = initialiseDatabase(app)
        database = dbInfo.first
        dbDriver = dbInfo.second

        val fcmCredential = FileInputStream(resourceFile("services/fcm_service_account.json"))

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(fcmCredential))
            .build()

        FirebaseApp.initializeApp(options)

        apnsClient = ApnsClientBuilder()
            .setApnsServer(
                ApnsClientBuilder.DEVELOPMENT_APNS_HOST
//                    ApnsClientBuilder.PRODUCTION_APNS_HOST
            )
            .setSigningKey(
                ApnsSigningKey.loadFromPkcs8File(
                    resourceFile("services/apns_key.p8"),
                    environment.configProperty("social.apple.teamId"),
                    environment.configProperty("social.apple.apnsKeyId"),
                )
            )
            .build()
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
                val maybeUnread = database.lastReadMessagesQueries.selectByPeerId(message.peerId)
                    .executeAsList()
                var chatUnreadCount = 0
                val totalUnreadCount = maybeUnread.sumOf { lastReadMessage ->
                    database.chatMessagesQueries.unreadCount(
                        fromId = lastReadMessage.fromId,
                        peerId = lastReadMessage.peerId,
                    ).executeAsOne().toInt()
                        .also {
                            if (message.fromId == lastReadMessage.fromId) {
                                chatUnreadCount = it
                            }
                        }
                }

                Notification.ChatMessage(
                    message = message,
                    senderName = senderName,
                    chatUnreadCount = chatUnreadCount,
                    totalUnreadCount = totalUnreadCount,
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
}