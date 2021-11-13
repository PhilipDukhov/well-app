package com.well.modules.models.chat

import com.well.modules.models.User
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class ChatMessage(
    val id: Id,
    val creation: Double,
    val fromId: User.Id,
    val peerId: User.Id,
    val content: Content,
) {
    @Serializable
    @JvmInline
    value class Id(val value: Long): Comparable<Id> {
        override fun toString() = value.toString()

        val isTmp get() = value < 0

        override fun compareTo(other: Id): Int =
            value.compareTo(other.value)

        object ColumnAdapter: com.squareup.sqldelight.ColumnAdapter<Id, Long> {
            override fun decode(databaseValue: Long) = Id(databaseValue)

            override fun encode(value: Id) = value.value
        }
    }

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

    fun secondId(currentId: User.Id) = when (currentId) {
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