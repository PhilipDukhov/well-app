package com.well.modules.db.chatMessages

import com.well.modules.utils.flowUtils.mapIterable
import com.well.modules.models.ChatMessageId
import com.well.modules.models.UserId
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChatPeerInfo(val lastMessage: ChatMessage, val unreadCount: Int)

fun ChatMessagesQueries.chatPeerInfos(id: UserId) =
    lastList(id)
        .asFlow()
        .mapToList()
        .map { list ->
            list
                .map(ChatMessages::toChatMessage)
                .map { message ->
                    ChatPeerInfo(
                        lastMessage = message,
                        unreadCount = if (message.fromId == id) 0 else
                            unreadCount(fromId = message.fromId, peerId = message.peerId)
                                .executeAsOne()
                                .toInt(),
                    )
                }
        }

fun ChatMessagesQueries.insert(message: ChatMessage) =
    message.run {
        insert(
            id = id,
            creation = creation,
            fromId = fromId,
            peerId = peerId,
            text = content.text,
            photoUrl = content.photoUrl,
            photoAspectRatio = content.photoAspectRatio,
        )
    }

fun LastReadMessagesQueries.insert(lastReadMessage: LastReadMessage) =
    insert(
        fromId = lastReadMessage.fromId,
        messageId = lastReadMessage.messageId,
        peerId = lastReadMessage.peerId
    )

fun ChatMessagesQueries.chatListFlow(currentUserId: UserId, peerId: UserId) =
    chatList(currentUserId, peerId)
        .asFlow()
        .mapToList()
        .mapIterable(ChatMessages::toChatMessage)

fun ChatMessagesQueries.messagePresenceFlow(): Flow<ChatMessageId> =
    newestChatMessageId()
        .asFlow()
        .mapToOneOrDefault(-1)

fun ChatMessagesDatabase.chatMessageWithStatusFlow(
    currentUid: ChatMessageId,
    peerUid: ChatMessageId,
) = chatMessagesQueries
    .chatList(firstId = currentUid, secondId = peerUid)
    .asFlow()
    .mapToList()
    .mapIterable(ChatMessages::toChatMessage)
    .toChatMessageWithStatusFlow(currentUid = currentUid, messagesDatabase = this)

fun ChatMessages.toChatMessage() = ChatMessage(
    id = id,
    creation = creation,
    fromId = fromId,
    peerId = peerId,
    content = if (photoUrl != null) ChatMessage.Content.Image(
        photoUrl,
        aspectRatio = photoAspectRatio
    ) else ChatMessage.Content.Text(text),
)

fun LastReadMessages.toLastReadMessage() = LastReadMessage(
    fromId = fromId,
    peerId = peerId,
    messageId = messageId,
)

fun ChatMessagesDatabase.insertTmpMessage(
    fromId: Int,
    peerId: Int,
    content: ChatMessage.Content,
) = transactionWithResult<ChatMessage> {
    chatMessagesQueries.insertTmp(
        fromId = fromId,
        peerId = peerId,
        text = content.text,
        photoUrl = content.photoUrl,
        photoAspectRatio = content.photoAspectRatio,
    )
    val message = chatMessagesQueries.getById(
        chatMessagesQueries.lastInsertId().executeAsOne().toInt()
    ).executeAsOne().toChatMessage()
    tmpMessageIdsQueries.updateTmpId(message.id)
    message
}