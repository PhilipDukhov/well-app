package com.well.modules.models.chat

import com.well.modules.models.date.Date
import com.well.modules.models.formatters.DateFormatter
import com.well.modules.models.formatters.formatChatMessage

class ChatMessageViewModel(
    val id: ChatMessage.Id,
    creation: Double,
    val content: ChatMessage.Content,
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

    val date = DateFormatter.formatChatMessage(Date(creation))

    companion object
}