package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
sealed class NotificationToken {
    @Serializable
    class Fcm(val token: String): NotificationToken()
    @Serializable
    class Apns(val token: String, val bundleId: String): NotificationToken()
}