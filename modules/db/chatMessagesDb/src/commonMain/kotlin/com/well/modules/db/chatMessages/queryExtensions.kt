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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class ChatPeerInfo(val lastMessage: ChatMessage, val unreadCount: Int)

fun ChatMessagesDatabase.chatPeerInfos(id: User.Id) =
    chatMessagesQueries
        .lastList(id)
        .asFlow()
        .mapToList()
        .map { list ->
            list
                .map {
                    it.toChatMessage(database = this)
                }
                .map { message ->
                    ChatPeerInfo(
                        lastMessage = message,
                        unreadCount = if (message.fromId == id) 0 else
                            chatMessagesQueries.unreadCount(
                                fromId = message.fromId,
                                peerId = message.peerId,
                            )
                                .executeAsOne()
                                .toInt(),
                    )
                }
        }

fun ChatMessagesDatabase.insert(message: ChatMessage) =
    transaction {
        message.run {
            chatMessagesQueries.insert(
                id = id,
                creation = creation,
                fromId = fromId,
                peerId = peerId,
                contentType = content.simpleType,
            )
        }
        insertContent(id = message.id, content = message.content)
    }

fun ChatMessagesDatabase.delete(messageId: ChatMessage.Id) =
    transaction {
        chatMessagesQueries.delete(messageId)
    }

fun LastReadMessagesQueries.insert(lastReadMessage: LastReadMessage) =
    insert(
        fromId = lastReadMessage.fromId,
        messageId = lastReadMessage.messageId,
        peerId = lastReadMessage.peerId
    )

fun ChatMessagesDatabase.chatListFlow(currentUserId: User.Id, peerId: User.Id) =
    chatMessagesQueries
        .chatList(currentUserId, peerId)
        .asFlow()
        .mapToList()
        .mapIterable { it.toChatMessage(this) }

fun ChatMessagesQueries.messagePresenceFlow(): Flow<ChatMessage.Id> =
    newestChatMessageId()
        .asFlow()
        .mapToOneOrDefault(ChatMessage.Id.undefined)

fun ChatMessagesDatabase.chatMessagesFlow(
    currentUid: User.Id,
    peerUid: User.Id,
) = chatMessagesQueries
    .chatList(firstId = currentUid, secondId = peerUid)
    .asFlow()
    .mapToList()
    .mapIterable {
        it.toChatMessage(database = this)
    }

fun ChatMessages.toChatMessage(database: ChatMessagesDatabase) = ChatMessage(
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
        ChatMessage.Content.SimpleType.Meeting -> {
            val content = database
                .chatContentMeetingsQueries
                .getById(id)
                .executeAsOne()
            ChatMessage.Content.Meeting(content.meetingId)
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
        contentType = content.simpleType,
    )
    val id = ChatMessage.Id(chatMessagesQueries.lastInsertId().executeAsOne())
    insertContent(id, content)
    tmpMessageIdsQueries.updateTmpId(id)
    chatMessagesQueries.getById(id)
        .executeAsOne()
        .toChatMessage(database = this@insertTmpMessage)
}

fun ChatMessagesDatabase.lastListFlow(uid: User.Id) =
    chatMessagesQueries
        .lastList(uid)
        .asFlow()
        .mapToList()
        .mapIterable {
            it.toChatMessage(database = this)
        }

fun LastReadMessagesQueries.select(
    fromAndPeerIds: Collection<Pair<User.Id, User.Id>>,
): Flow<List<LastReadMessages>> =
    if (fromAndPeerIds.isEmpty())
        flowOf(emptyList())
    else
        select(
            fromAndPeerIds = fromAndPeerIds
                .map { "${it.first}|${it.second}" }
        ).asFlow().mapToList()

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

fun ChatMessagesDatabase.markRead(id: ChatMessage.Id) {
    val message = chatMessagesQueries.getById(id).executeAsOneOrNull() ?: return
    val currentLast = lastReadMessagesQueries.selectSingle(
        fromId = message.fromId,
        peerId = message.peerId,
    ).executeAsOneOrNull() ?: return
    if (currentLast.messageId >= message.id) return
    lastReadMessagesQueries.insert(
        fromId = message.fromId,
        peerId = message.peerId,
        messageId = message.id
    )
}

private fun ChatMessagesDatabase.insertContent(
    id: ChatMessage.Id,
    content: ChatMessage.Content,
) {
    when (content) {
        is ChatMessage.Content.Image -> {
            chatContentImagesQueries.run {
                insert(
                    messageId = id,
                    url = content.url,
                    aspectRatio = content.aspectRatio,
                )
            }
            println("inserted ${
                chatContentImagesQueries
                    .getById(id)
                    .executeAsOne()
            }")
        }
        is ChatMessage.Content.Meeting -> {
            chatContentMeetingsQueries.run {
                insert(
                    messageId = id,
                    meetingId = content.meetingId,
                )
            }
        }
        is ChatMessage.Content.Text -> {
            chatContentTextsQueries.run {
                insert(
                    messageId = id,
                    text = content.string,
                )
            }
        }
    }
}