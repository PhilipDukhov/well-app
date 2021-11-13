package com.well.modules.db.chatMessages

import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import com.well.modules.utils.flowUtils.flattenFlow
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ChatPeerInfo(val lastMessage: ChatMessage, val unreadCount: Int)

fun ChatMessagesQueries.chatPeerInfos(id: User.Id) =
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

fun ChatMessagesQueries.chatListFlow(currentUserId: User.Id, peerId: User.Id) =
    chatList(currentUserId, peerId)
        .asFlow()
        .mapToList()
        .mapIterable(ChatMessages::toChatMessage)

fun ChatMessagesQueries.messagePresenceFlow(): Flow<ChatMessage.Id> =
    newestChatMessageId()
        .asFlow()
        .mapToOneOrDefault(ChatMessage.Id(-1))

fun ChatMessagesDatabase.chatMessageWithStatusFlow(
    currentUid: User.Id,
    peerUid: User.Id,
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
    fromId: User.Id,
    peerId: User.Id,
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
        ChatMessage.Id(chatMessagesQueries.lastInsertId().executeAsOne())
    ).executeAsOne().toChatMessage()
    tmpMessageIdsQueries.updateTmpId(message.id)
    message
}

fun ChatMessagesQueries.lastListFlow(uid: User.Id) =
    lastList(uid)
        .asFlow()
        .mapToList()

fun ChatMessagesDatabase.lastListWithStatusFlow(uid: User.Id) =
    chatMessagesQueries.lastListFlow(uid)
        .mapIterable(ChatMessages::toChatMessage)
        .toChatMessageWithStatusFlow(currentUid = uid, messagesDatabase = this)

fun ChatMessagesQueries.unreadCountFlow(uid: User.Id, message: ChatMessage) =
    unreadCount(fromId = message.secondId(uid), peerId = uid)
        .asFlow()
        .mapToOne()
        .map { unreadCount ->
            message.id to unreadCount
        }

fun ChatMessagesQueries.unreadCountsFlow(uid: User.Id, messages: List<ChatMessage>) =
    messages
        .map { unreadCountFlow(uid, it) }
        .flattenFlow()
        .map { it.toMap() }

fun LastReadMessagesQueries.selectAllFlow() =
    selectAll()
        .asFlow()
        .mapToList()
        .mapIterable(LastReadMessages::toLastReadMessage)