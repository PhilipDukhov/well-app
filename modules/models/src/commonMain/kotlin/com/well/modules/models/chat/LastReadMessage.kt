package com.well.modules.models.chat

import com.well.modules.models.User
import kotlinx.serialization.Serializable

@Serializable
data class LastReadMessage(
    val fromId: User.Id,
    val peerId: User.Id,
    val messageId: ChatMessage.Id,
)