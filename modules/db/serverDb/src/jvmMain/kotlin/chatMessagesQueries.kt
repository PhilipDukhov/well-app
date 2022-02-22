package com.well.modules.db.server

import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import java.util.*

fun Database.insertChatMessage(message: ChatMessage) =
    insertChatMessage(
        fromId = message.fromId,
        peerId = message.peerId,
        content = message.content,
    )

fun Database.insertChatMessage(
    fromId: User.Id,
    peerId: User.Id,
    content: ChatMessage.Content,
): ChatMessage.Id =
    transactionWithResult {
        val messageId = with(chatMessagesQueries) {
            insert(
                creation = Date().time.toDouble() / 1000,
                fromId = fromId,
                peerId = peerId,
                contentType = content.simpleType,
            )
            ChatMessage.Id(lastInsertId().executeAsOne())
        }
        when (content) {
            is ChatMessage.Content.Image -> {
                chatContentImagesQueries
                    .insert(
                        ChatContentImages(
                            messageId = messageId,
                            url = content.url,
                            aspectRatio = content.aspectRatio,
                        )
                    )
            }
            is ChatMessage.Content.Text -> {
                chatContentTextsQueries
                    .insert(
                        ChatContentTexts(
                            messageId = messageId,
                            text = content.string,
                        )
                    )
            }
        }
        messageId
    }

fun ChatMessages.toChatMessage(database: Database): ChatMessage {
    return ChatMessage(
        id = id,
        creation = creation,
        fromId = fromId,
        peerId = peerId,
        content = when (contentType) {
            ChatMessage.Content.SimpleType.Image -> {
                val content = database
                    .chatContentImagesQueries
                    .getById(id)
                    .executeAsOne()
                ChatMessage.Content.Image(content.url, content.aspectRatio)
            }
            ChatMessage.Content.SimpleType.Text -> {
                val content = database
                    .chatContentTextsQueries
                    .getById(id)
                    .executeAsOne()
                ChatMessage.Content.Text(content.text)
            }
        }
    )
}

fun ChatMessagesQueries.peerIdsListFlow(id: User.Id) =
    peerIdsList(id = id)
        .asFlow()
        .mapToList()

fun ChatMessagesQueries.getAllForUserFlow(id: User.Id, lastPresentedId: ChatMessage.Id) =
    getAllForUser(
        id = id,
        lastPresentedId = lastPresentedId,
    )
        .asFlow()
        .mapToList()