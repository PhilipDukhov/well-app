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

        companion object {
            val undefined = Id(Long.MIN_VALUE)
        }
    }

    @Serializable
    sealed class Content {
        enum class SimpleType {
            Image,
            Meeting,
            Text,
        }

        val simpleType get() = SimpleType.valueOf(this::class.simpleName!!)

        val descriptionText get() =
            when (this) {
                is Image -> "photo"
                is Text -> string
                is Meeting -> {
                    TODO("remove")
                }
            }

        @Serializable
        data class Text(val string: String) : Content()

        @Serializable
        data class Meeting(val meetingId: com.well.modules.models.Meeting.Id) : Content()

        @Serializable
        data class Image(val url: String, val aspectRatio: Float) : Content()
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
}