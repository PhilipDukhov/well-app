package com.well.modules.db.server

import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import java.util.Date

fun ChatMessagesQueries.insertChatMessage(message: ChatMessage): ChatMessage =
    transactionWithResult {
        insert(
            creation = Date().time.toDouble() / 1000,
            fromId = message.fromId,
            peerId = message.peerId,
            text = message.content.text,
            photoUrl = message.content.photoUrl,
            photoAspectRatio = message.content.photoAspectRatio,
        )
        val id = lastInsertId()
            .executeAsOne()
            .toInt()
        getById(id)
            .executeAsOne()
            .toChatMessage()
    }

fun ChatMessages.toChatMessage() = ChatMessage(
    id = id,
    creation = creation,
    fromId = fromId,
    peerId = peerId,
    content = if (photoUrl != null) ChatMessage.Content.Image(photoUrl, aspectRatio = photoAspectRatio) else ChatMessage.Content.Text(text),
)

fun LastReadMessages.toLastReadMessage() = LastReadMessage(
    fromId = fromId,
    messageId = messageId,
    peerId = peerId,
)
