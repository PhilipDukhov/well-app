package com.well.server.utils.notifications

import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.models.Notification
import com.well.modules.models.NotificationToken
import com.well.modules.models.User
import com.well.modules.utils.kotlinUtils.UUID
import com.well.server.utils.Dependencies
import com.eatthepath.pushy.apns.DeliveryPriority
import com.eatthepath.pushy.apns.PushType
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

suspend fun sendNotification(
    notification: Notification,
    tokenInfo: SelectTokenByUid,
    dependencies: Dependencies,
) {
    when (val token = tokenInfo.token) {
        is NotificationToken.Fcm -> {
            sendFcmNotification(
                notification = notification,
                token = token.token
            )
        }
        is NotificationToken.Apns -> {
            sendApnsNotification(
                notification = notification,
                token = token.token,
                bundleId = token.bundleId,
                dependencies = dependencies
            )
        }
    }
}

private fun sendFcmNotification(
    notification: Notification,
    token: String,
) {
    try {
        val message = Message.builder()
            .putData(Notification.payloadDataKey, Json.encodeToString(notification))
            .setToken(token)
            .build()
        val res = FirebaseMessaging.getInstance().send(message, false)
        println("sent ok $res")
    } catch (firebaseException: FirebaseMessagingException) {
        when (firebaseException.messagingErrorCode) {
            MessagingErrorCode.UNREGISTERED,
            MessagingErrorCode.INVALID_ARGUMENT,
            -> {
                println(" invalid token $firebaseException ${firebaseException.messagingErrorCode}")
                println(firebaseException.cause?.stackTraceToString())
            }
            else -> {
                throw firebaseException
            }
        }
    }
}

suspend fun sendVoipApnsNotification(
    tokenInfo: NotificationToken.Apns,
    dependencies: Dependencies,
): String {
    val notification = Notification.IncomingCall(
        callId = UUID(),
        userId = User.Id(value = 0),
        hasVideo = false,
        senderName = "Phil sendVoipApnsNotification",
        totalUnreadCount = 0,
    )
    val payload = mapOf(Notification.payloadDataKey to notification)
    val pushNotification = SimpleApnsPushNotification(
        /* token = */
        tokenInfo.token,
        /* topic = */
        "${tokenInfo.bundleId}.voip",
        /* payload = */
        Json.encodeToString(payload),
        /* invalidationTime = */
        Instant.now(),
        /* priority = */
        DeliveryPriority.IMMEDIATE,
        /* pushType = */
        PushType.VOIP,
        /* collapseId = */
        null,
        /* apnsId = */
        null,
    )
    val response = dependencies
        .run {
            if (tokenInfo.bundleId == "com.well.app")
                prodApnsClient
            else
                devApnsClient
        }
        .sendNotification(pushNotification)
        .await()
    return "${response.apnsId} rejectionReason ${response.rejectionReason}"
}

private suspend fun sendApnsNotification(
    notification: Notification,
    token: String,
    bundleId: String,
    dependencies: Dependencies,
) {
    val payloadBuilder = SimpleApnsPayloadBuilder()
    payloadBuilder.setAlertTitle(notification.alertTitle)
    payloadBuilder.setAlertBody(notification.alertBody)
    payloadBuilder.setBadgeNumber(notification.totalUnreadCount)
    payloadBuilder.addCustomProperty(Notification.payloadDataKey, Json.encodeToString(notification))
    val payload = payloadBuilder.build()
    val pushNotification = SimpleApnsPushNotification(
        /* token = */
        token,
        /* topic = */
        bundleId,
        /* payload = */
        payload,
        /* invalidationTime = */
        Instant.now().plus(SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD),
        /* priority = */
        DeliveryPriority.IMMEDIATE,
        /* pushType = */
        null,
        /* collapseId = */
        null,
        /* apnsId = */
        null,
    )
    val response = dependencies
        .run {
            if (bundleId == "com.well.app")
                prodApnsClient
            else
                devApnsClient
        }
        .sendNotification(pushNotification)
        .await()
    println("${response.apnsId} rejectionReason ${response.rejectionReason}")
}