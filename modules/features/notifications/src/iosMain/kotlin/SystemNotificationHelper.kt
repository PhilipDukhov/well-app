package com.well.modules.features.notifications

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.models.Notification
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.RawNotification
import io.github.aakira.napier.Napier
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.UserNotifications.UNUserNotificationCenter

internal actual class SystemNotificationHelper actual constructor(
    private val applicationContext: ApplicationContext,
) {
    actual fun updateMessageNotification(notification: Notification.ChatMessage) {
    }

    actual fun updateTotalUnreadCounter(counter: Int) {
        MainScope().launch {
            applicationContext.application.applicationIconBadgeNumber = counter.toLong()
        }
    }

    actual fun getNotificationPayloadString(rawNotification: RawNotification): String =
        rawNotification.request.content.userInfo[Notification.payloadDataKey] as String

    actual fun deleteNotification(notification: Notification.ChatMessage) {
        Napier.i("1 $notification")
        val idsToRemove = mutableListOf<String>()
        if (
            storedNotifications.removeAll {
                Napier.i("check $it")
                val (storedRawNotification, storedNotification) = it
                if (notification.message.id == (storedNotification as? Notification.ChatMessage)?.message?.id) {
                    idsToRemove.add(storedRawNotification.request.identifier)
                    Napier.i("toRemove")
                    true
                } else {
                    Napier.i("not")
                    false
                }
            }
        ) {
            Napier.i("removing $idsToRemove")
            UNUserNotificationCenter
                .currentNotificationCenter()
                .removeDeliveredNotificationsWithIdentifiers(idsToRemove)
        }
    }

    actual fun clearAllNotifications() {
        UNUserNotificationCenter
            .currentNotificationCenter()
            .removeAllDeliveredNotifications()
    }

    private val storedNotifications = AtomicMutableList<Pair<RawNotification, Notification>>()
    actual fun storeNotificationWithRaw(
        notification: Notification,
        rawNotification: RawNotification,
    ) {
        storedNotifications.add(rawNotification to notification)
    }
}