package com.well.modules.features.notifications

import com.well.modules.models.Notification
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.RawNotification

internal expect class SystemNotificationHelper(
    applicationContext: ApplicationContext,
) {
    fun storeNotificationWithRaw(notification: Notification, rawNotification: RawNotification)
    fun getNotificationPayloadString(rawNotification: RawNotification) : String
    fun updateMessageNotification(notification: Notification.ChatMessage)
    fun updateTotalUnreadCounter(counter: Int)
    fun deleteNotification(notification: Notification.ChatMessage)
    fun clearAllNotifications()
}