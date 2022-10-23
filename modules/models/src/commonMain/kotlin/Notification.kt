package com.well.modules.models

import com.well.modules.utils.kotlinUtils.UUID
import com.well.modules.models.Meeting.State as MeetingState
import kotlinx.serialization.Serializable

@Serializable
sealed class Notification {
    companion object {
        const val payloadDataKey = "SerializableNotification"
    }

    abstract val totalUnreadCount: Int
    abstract val senderName: String
    abstract val alertTitle: String
    abstract val alertBody: String

    @Serializable
    sealed class Voip: Notification() {
        @Serializable
        class IncomingCall(
            val webSocketMsg: WebSocketMsg.Back.IncomingCall,
            override val totalUnreadCount: Int,
        ) : Voip(), CallInfo by webSocketMsg {
            override val senderName: String
                get() = user.fullName
            override val alertTitle: String = "$senderName is calling"
            override val alertBody: String = ""
            override val id: CallId get() = webSocketMsg.callId
        }

        @Serializable
        class CanceledCall(
            val callId: CallId,
            override val senderName: String,
            override val totalUnreadCount: Int,
        ) : Voip() {
            override val alertTitle: String = ""
            override val alertBody: String = ""
        }
    }

    @Serializable
    data class ChatMessage(
        val message: com.well.modules.models.chat.ChatMessage,
        val chatUnreadCount: Int,
        override val senderName: String,
        override val totalUnreadCount: Int,
    ) : Notification() {
        override val alertTitle get() = senderName
        override val alertBody get() = message.content.descriptionText
    }

    @Serializable
    class Meeting(
        val meeting: com.well.modules.models.Meeting,
        override val senderName: String,
        override val totalUnreadCount: Int,
    ) : Notification() {
        override val alertTitle
            get() = when (meeting.state) {
                MeetingState.Requested -> {
                    "New meeting request"
                }
                MeetingState.Confirmed -> {
                    "Meeting confirmed"
                }
                is MeetingState.Rejected -> {
                    "Meeting rejected"
                }
                is MeetingState.Canceled -> {
                    "Meeting canceled"
                }
            }

        override val alertBody: String
            get() {
                val dateTimeDescription = meeting.dateTimeDescription
                return when (meeting.state) {
                    MeetingState.Requested -> {
                        "$senderName sent a request for a meeting $dateTimeDescription.\nAccept it now!"
                    }
                    MeetingState.Confirmed -> {
                        "$senderName confirmed a meeting with you $dateTimeDescription"
                    }
                    is MeetingState.Rejected -> {
                        "$senderName rejected a meeting with you $dateTimeDescription with the following reason: ${meeting.state.reason}"
                    }
                    is MeetingState.Canceled -> {
                        "$senderName cancelled a meeting with you $dateTimeDescription with the following reason: ${meeting.state.reason}"
                    }
                }
            }
    }
}