package com.well.modules.models.chat

import com.well.modules.models.date.Date
import com.well.modules.models.formatters.DateFormatter
import com.well.modules.models.formatters.formatChatMessage

class ChatMessageViewModel(
    val id: ChatMessage.Id,
    creation: Double,
    val content: Content,
    val status: Status,
) {
    enum class Status {
        OutgoingSending,
        OutgoingSent,
        OutgoingRead,
        IncomingUnread,
        IncomingRead,
        ;

        val isIncoming: Boolean
            get() = when (this) {
                IncomingUnread, IncomingRead -> true
                else -> false
            }
    }
    sealed class Content {
        data class Text(val string: String) : Content()
        data class Meeting(val meeting: com.well.modules.models.Meeting?) : Content()
        data class Image(val url: String, val aspectRatio: Float) : Content()

        companion object {
            fun create(
                content: ChatMessage.Content,
                getMeeting: (com.well.modules.models.Meeting.Id) -> com.well.modules.models.Meeting?,
            ) = when (content) {
                is ChatMessage.Content.Image -> {
                    Image(
                        url = content.url,
                        aspectRatio = content.aspectRatio
                    )
                }
                is ChatMessage.Content.Meeting -> {
                    Meeting(getMeeting(content.meetingId))
                }
                is ChatMessage.Content.Text -> {
                    Text(content.string)
                }
            }
        }
    }

    val date = DateFormatter.formatChatMessage(Date(creation))

    companion object
}