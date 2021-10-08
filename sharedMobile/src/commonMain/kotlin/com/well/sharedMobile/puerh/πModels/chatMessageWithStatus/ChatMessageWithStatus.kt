package com.well.sharedMobile.puerh.πModels.chatMessageWithStatus

import com.well.modules.models.Date
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.formatters.DateFormatter
import com.well.modules.models.formatters.formatChatMessage
import kotlinx.serialization.Serializable

data class ChatMessageWithStatus(
    val message: ChatMessage,
    val status: Status,
) {
    @Serializable
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

    val date = DateFormatter.formatChatMessage(Date(message.creation))

    companion object
}
