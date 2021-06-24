package com.well.modules.models.chat

import com.well.modules.models.ChatMessageId
import com.well.modules.models.UserId
import kotlinx.serialization.Serializable

@Serializable
data class LastReadMessage(
    val fromId: UserId,
    val peerId: UserId,
    val messageId: ChatMessageId,
)