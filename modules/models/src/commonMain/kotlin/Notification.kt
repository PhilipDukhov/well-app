package com.well.modules.models

import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import kotlinx.serialization.Serializable
import com.well.modules.models.Meeting.State as MeetingState

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
    data class Meeting(
        val meeting: com.well.modules.models.Meeting,
        override val senderName: String,
        override val totalUnreadCount: Int,
    ) : Notification() {
        override val alertTitle get() = when (meeting.state) {
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
                val date = "on ${meeting.startDay.localizedDayAndShortMonth()} at ${meeting.startTime}"
                return when (meeting.state) {
                    MeetingState.Requested -> {
                        "$senderName sent a request for a meeting $date.\nAccept it now!"
                    }
                    MeetingState.Confirmed -> {
                        "$senderName confirmed a meeting with you $date"
                    }
                    is MeetingState.Rejected -> {
                        "$senderName rejected a meeting with you $date with the following reason: ${meeting.state.reason}"
                    }
                    is MeetingState.Canceled -> TODO()
                }
            }
    }
}