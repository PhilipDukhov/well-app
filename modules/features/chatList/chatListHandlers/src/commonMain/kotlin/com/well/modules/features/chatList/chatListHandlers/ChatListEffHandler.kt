package com.well.modules.features.chatList.chatListHandlers

import com.well.modules.db.chatMessages.ChatMessages
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.LastReadMessages
import com.well.modules.db.chatMessages.messagePresenceFlow
import com.well.modules.db.chatMessages.toChatMessage
import com.well.modules.db.chatMessages.toChatMessageWithStatusFlow
import com.well.modules.db.chatMessages.toLastReadMessage
import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Eff
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Msg
import com.well.modules.models.ChatMessageId
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.combineToUnit
import com.well.modules.utils.flowUtils.flattenFlow
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatListEffHandler(
    private val currentUid: UserId,
    private val services: Services,
    private val messagesDatabase: ChatMessagesDatabase,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    data class Services(
        val openUserChat: (id: UserId) -> Unit,
        val onConnectedFlow: Flow<Unit>,
        val sendFrontWebSocketMsg: suspend (WebSocketMsg.Front) -> Unit,
        val getUsersByIds: (List<UserId>) -> Flow<List<User>>,
    )

    private val messagesFlow =
        messagesDatabase
            .chatMessagesQueries
            .lastList(currentUid)
            .asFlow()
            .mapToList()

    private val messagesWithStatusFlow = messagesFlow
        .mapIterable(ChatMessages::toChatMessage)
        .toChatMessageWithStatusFlow(currentUid = currentUid, messagesDatabase = messagesDatabase)

    private val chatListItemsFlow = messagesWithStatusFlow
        .flatMapLatest { chatMessagesWithStatus ->
            val unreadCountsFlow = chatMessagesWithStatus.map { message ->
                messagesDatabase.chatMessagesQueries
                    .unreadCount(fromId = message.message.secondId(currentUid), peerId = currentUid)
                    .asFlow()
                    .mapToOne()
                    .map { unreadCount ->
                        message.message.id to unreadCount
                    }
            }
                .flattenFlow()
                .map { it.toMap() }
            services.getUsersByIds(chatMessagesWithStatus.map { it.message.secondId(currentUid) })
                .combine(unreadCountsFlow) { users, unreadCounts ->
                    chatMessagesWithStatus.mapNotNull { messageWithStatus ->
                        ChatListFeature.State.ListItem(
                            user = users.firstOrNull {
                                it.id == messageWithStatus.message.secondId(currentUid)
                            } ?: return@mapNotNull null,
                            lastMessage = messageWithStatus,
                            unreadCount = unreadCounts[messageWithStatus.message.id]?.toInt()
                                ?: 0,
                        )
                    }
                }
        }
    private val lastPresentMessageIdFlow: Flow<ChatMessageId> =
        messagesDatabase.chatMessagesQueries
            .messagePresenceFlow()
    private val lastReadPresenceFlow =
        messagesDatabase.lastReadMessagesQueries
            .selectAll()
            .asFlow()
            .mapToList()
            .mapIterable(LastReadMessages::toLastReadMessage)

    init {
        coroutineScope.launch {
            chatListItemsFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { chatList ->
                    listener(Msg.UpdateItems(chatList))
                }
        }
        coroutineScope.launch {
            lastPresentMessageIdFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { lastPresentMessageId ->
                    Napier.i("WebSocketMsg.Front.SetChatMessagePresence $lastPresentMessageId")
                    services.sendFrontWebSocketMsg(
                        WebSocketMsg.Front.SetChatMessagePresence(
                            messagePresenceId = lastPresentMessageId
                        )
                    )
                }
        }
        coroutineScope.launch {
            lastReadPresenceFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { lastReadPresence ->
                    services.sendFrontWebSocketMsg(
                        WebSocketMsg.Front.UpdateChatReadStatePresence(
                            lastReadPresence
                        )
                    )
                }
//            networkManager.onConnectedFlow
//                .collect {
//                    val lastReadMessages = messagesDatabase.lastReadMessagesQueries
//                        .selectAll()
//                        .executeAsList()
//                        .map(LastReadMessages::toLastReadMessage)
//                    println("lastReadPresence $lastReadMessages")
//                    networkManager.send(
//                        WebSocketMsg.Front.UpdateChatReadStatePresence(
//                            lastReadMessages
//                        )
//                    )
//                }
        }
    }

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            is Eff.SelectChat -> {
                services.openUserChat(eff.uid)
            }
        }
    }
}