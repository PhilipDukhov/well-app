package com.well.modules.models.chat

import com.well.modules.models.ChatMessageId
import com.well.modules.models.UserId
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: ChatMessageId,
    val creation: Double,
    val fromId: UserId,
    val peerId: UserId,
    val content: Content,
) {
    @Serializable
    sealed class Content {
        val text: String
            get() = (this as? Text)?.string ?: ""
        val photoUrl: String?
            get() = (this as? Image)?.url
        open val photoAspectRatio: Float?
            get() = (this as? Image)?.aspectRatio

        @Serializable
        data class Text(val string: String) : Content()

        @Serializable
        data class Image(val url: String, val aspectRatio: Float? = null) : Content()
    }

    fun secondId(currentId: UserId) = when (currentId) {
        fromId -> {
            peerId
        }
        peerId -> {
            fromId
        }
        else -> {
            error("ChatMessage secondId failure: currentId=$currentId, this=$this")
        }
    }

    fun contentDescription() = when (content) {
        is Content.Image -> "photo"
        is Content.Text -> content.text
    }
}