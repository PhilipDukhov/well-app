package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
sealed class NotificationToken {
    @Serializable
    class Fcm(val token: String): NotificationToken()
    @Serializable

    data class Apns(val notificationToken: String?, val voipToken: String?, val bundleId: String): NotificationToken()

    // helper containers
    class ApnsNotification(val notificationToken: String, val bundleId: String)

    class ApnsVoip(val voipToken: String?, val bundleId: String)
}