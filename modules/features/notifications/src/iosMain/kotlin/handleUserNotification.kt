package com.well.modules.features.notifications

import com.well.modules.models.Notification
import io.github.aakira.napier.Napier
import platform.UserNotifications.UNNotificationResponse

fun NotificationHandler.handleNotificationResponse(response: UNNotificationResponse) {
    try {
        Napier.i("1")
        val notification = parseRawNotification(response.notification)
        Napier.i("parsed $notification")
        when (notification) {
            is Notification.ChatMessage -> {
                services.openChat(notification.message.fromId)
            }
        }
    } catch (t: Throwable) {
        Napier.e("handleNotificationResponse", t)
    }
}