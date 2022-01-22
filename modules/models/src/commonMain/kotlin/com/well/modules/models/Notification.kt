package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Notification {
    companion object {
        const val payloadDataKey = "SerializableNotification"
    }

    abstract val totalUnreadCount: Int

    @Serializable
    data class ChatMessage(
        val message: com.well.modules.models.chat.ChatMessage,
        val senderName: String,
        val chatUnreadCount: Int,
        override val totalUnreadCount: Int,
    ) : Notification()

    @Serializable
    data class Meeting(
        val meetingId: com.well.modules.models.Meeting.Id,
        val senderName: String,
        val chatUnreadCount: Int,
        override val totalUnreadCount: Int,
    ) : Notification()
}