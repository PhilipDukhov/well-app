package com.well.modules.features.notifications

import com.well.modules.models.Notification
import io.github.aakira.napier.Napier
import platform.UserNotifications.UNNotificationResponse

fun NotificationHandler.handleNotificationResponse(response: UNNotificationResponse) {
    try {
        when (val notification = parseRawNotification(response.notification)) {
            is Notification.ChatMessage -> {
                services.openChat(notification.message.fromId)
            }
            is Notification.Meeting -> {
                services.openMeeting(notification.meeting.id)
            }
        }
    } catch (t: Throwable) {
        Napier.e("handleNotificationResponse", t)
    }
}