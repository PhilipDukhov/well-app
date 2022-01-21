package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
sealed class NotificationToken {
    @Serializable
    data class Fcm(val token: String): NotificationToken()
    @Serializable
    data class Apns(val token: String, val bundleId: String): NotificationToken()
}