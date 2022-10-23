package com.well.server.utils.notifications

import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.models.Notification
import com.well.modules.models.NotificationToken
import com.well.server.utils.Services
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
    services: Services,
) {
    when (val token = tokenInfo.token) {
        is NotificationToken.Fcm -> {
            sendFcmNotification(
                notification = notification,
                token = token
            )
        }
        is NotificationToken.Apns -> {
            sendApnsNotification(
                notification = notification,
                token = token,
                services = services,
            )
        }
    }
}

private fun sendFcmNotification(
    notification: Notification,
    token: NotificationToken.Fcm,
) {
    try {
        val message = Message.builder()
            .putData(Notification.payloadDataKey, Json.encodeToString(notification))
            .setToken(token.token)
            .build()
        val res = FirebaseMessaging.getInstance().send(message, false)
        println("sent ok $res")
    } catch (firebaseException: FirebaseMessagingException) {
        when (firebaseException.messagingErrorCode) {
            MessagingErrorCode.UNREGISTERED,
            MessagingErrorCode.INVALID_ARGUMENT,
            -> {
                println("invalid token $firebaseException ${firebaseException.messagingErrorCode}")
                println(firebaseException.cause?.stackTraceToString())
            }
            else -> {
                throw firebaseException
            }
        }
    }
}

private suspend fun sendApnsNotification(
    notification: Notification,
    token: NotificationToken.Apns,
    services: Services,
) {
    val rawToken: String
    val topic: String
    val invalidationTime: Instant
    val pushType: PushType
    val payload: String

    if (notification is Notification.Voip) {
        rawToken = token.voipToken ?: return
        topic = "${token.bundleId}.voip"
        invalidationTime = Instant.now()
        pushType = PushType.VOIP
        payload = Json.encodeToString(
            mapOf(Notification.payloadDataKey to notification)
        )
    } else {
        rawToken = token.notificationToken ?: return
        topic = token.bundleId
        invalidationTime = Instant.now().plus(SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD)
        pushType = PushType.ALERT
        val payloadBuilder = SimpleApnsPayloadBuilder()
        payloadBuilder.setAlertTitle(notification.alertTitle)
        payloadBuilder.setAlertBody(notification.alertBody)
        payloadBuilder.setBadgeNumber(notification.totalUnreadCount)
        payloadBuilder.addCustomProperty(Notification.payloadDataKey, Json.encodeToString(notification))
        payload = payloadBuilder.build()
    }

    val pushNotification = SimpleApnsPushNotification(
        /* token = */
        rawToken,
        /* topic = */
        topic,
        /* payload = */
        payload,
        /* invalidationTime = */
        invalidationTime,
        /* priority = */
        DeliveryPriority.IMMEDIATE,
        /* pushType = */
        pushType,
        /* collapseId = */
        null,
        /* apnsId = */
        null,
    )
    println("send $pushNotification")
    val response = services
        .run {
            if (token.bundleId == "com.well.app")
                prodApnsClient
            else
                devApnsClient
        }
        .sendNotification(pushNotification)
        .await()
    println("${response.apnsId} rejectionReason ${response.rejectionReason}")
}