package com.well.modules.features.notifications

import com.well.modules.models.Notification
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.RawNotification
import android.app.Notification as AndroidNotification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

internal actual class SystemNotificationHelper actual constructor(
    private val applicationContext: ApplicationContext,
) {
    companion object {
        const val openChatAction = "notification_open_chat_action"
        const val userIdKey = "user_id_key"
    }

    private val manager by lazy { NotificationManagerCompat.from(applicationContext.context) }

    actual fun updateMessageNotification(notification: Notification.ChatMessage) {
        if (!manager.areNotificationsEnabled()) return
        val style = NotificationCompat.BigTextStyle()
        style.setBigContentTitle(
            if (notification.chatUnreadCount > 1)
                "(${notification.chatUnreadCount}) ${notification.senderName}"
            else
                notification.senderName
        )
        style.bigText(notification.message.content.descriptionText)
        val channelId = "chat_msg_${notification.message.fromId}"
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && manager.notificationChannels.all { it.id != channelId }
        ) {
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    channelId,
                    NotificationManagerCompat.IMPORTANCE_HIGH,
                ).setName(notification.senderName)
                    .setShowBadge(true)
                    .build()
            )
        }
        manager.notify(
            notification.message.id.value.toInt(),
            NotificationCompat.Builder(
                /* context = */ applicationContext.context,
                /* channelId = */ channelId
            )
                .setDefaults(AndroidNotification.DEFAULT_ALL)
                .setSmallIcon(applicationContext.notificationResId)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(
                    PendingIntent.getActivity(
                        /* context = */
                        applicationContext.context,
                        /* requestCode = */
                        0,
                        /* intent = */
                        Intent(applicationContext.context, applicationContext.activityClass).apply {
                            action = openChatAction
                            putExtra(userIdKey, notification.message.fromId.value)
                        },
                        /* flags = */
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                )
                .setOnlyAlertOnce(true)
                .setStyle(style)
                .setNumber(notification.chatUnreadCount)
                .build()
        )
    }

    actual fun getNotificationPayloadString(rawNotification: RawNotification): String =
        rawNotification.data[Notification.payloadDataKey]!!

    actual fun updateTotalUnreadCounter(counter: Int) {
        // not supported on Android
    }

    actual fun deleteNotification(notification: Notification.ChatMessage) {
        manager.cancel(notification.message.id.value.toInt())
    }

    actual fun clearAllNotifications() {
        manager.cancelAll()
    }

    actual fun storeNotificationWithRaw(
        notification: Notification,
        rawNotification: RawNotification,
    ) {
    }
}