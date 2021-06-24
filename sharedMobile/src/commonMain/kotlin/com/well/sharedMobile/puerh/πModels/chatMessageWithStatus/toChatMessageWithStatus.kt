package com.well.sharedMobile.puerh.πModels.chatMessageWithStatus

import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.flowHelper.asSingleFlow
import com.well.modules.flowHelper.mapIterable
import com.well.modules.models.UserId
import com.well.modules.models.chat.ChatMessage
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.ChatMessageWithStatus.Status
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private sealed class ChatMessageStatusContainer(open val message: ChatMessage) {
    data class Status(val status: ChatMessageWithStatus.Status, override val message: ChatMessage) :
        ChatMessageStatusContainer(message)

    data class Sent(override val message: ChatMessage) : ChatMessageStatusContainer(message)
}

fun Flow<List<ChatMessage>>.toChatMessageWithStatusFlow(
    currentUid: UserId,
    messagesDatabase: ChatMessagesDatabase,
) = flatMapLatest { chatList ->
    println("toChatMessageWithStatusFlow $chatList")
    val containers = chatList
        .map { message ->
            when {
                message.peerId == currentUid -> {
                    ChatMessageStatusContainer.Status(Status.IncomingRead, message)
                }
                message.id < 0 -> {
                    ChatMessageStatusContainer.Status(Status.OutgoingSending, message)
                }
                else -> {
                    ChatMessageStatusContainer.Sent(message)
                }
            }
        }
    val sentContainers = containers
        .mapNotNull { it as? ChatMessageStatusContainer.Sent }
    val statusContainers = containers.mapNotNull { it as? ChatMessageStatusContainer.Status }
    if (sentContainers.isNotEmpty())
        messagesDatabase
            .lastReadMessagesQueries
            .select(
                fromAndPeerIds =
                sentContainers
                    .groupBy {
                        it.message.peerId to it.message.fromId
                    }
                    .map {
                        "${it.value.first().message.peerId}|$currentUid"
                }
            )
            .asFlow()
            .mapToList()
            .map { lastReadMessages ->
                val lastReadContainers = sentContainers
                    .map { container ->
                        val lastReadMessageId = lastReadMessages
                            .firstOrNull { it.peerId == container.message.peerId }
                            ?.messageId
                        ChatMessageStatusContainer.Status(
                            status = if (container.message.id <= lastReadMessageId ?: -1)
                                Status.OutgoingRead
                            else
                                Status.OutgoingSent,
                            message = container.message
                        )
                    }
                statusContainers + lastReadContainers
            }
    else {
        statusContainers.asSingleFlow()
    }
        .mapIterable {
            ChatMessageWithStatus(
                message = it.message,
                status = it.status,
            )
        }
        .map { messages -> messages.sortedByDescending { it.message.creation } }
}