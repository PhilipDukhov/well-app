package com.well.modules.features.chatList.chatListHandlers

import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Eff
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Msg
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageContainer
import com.well.modules.models.chat.LastReadMessage
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.combineWithUnit
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

class ChatListEffHandler(
    private val currentUid: User.Id,
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    data class Services(
        val openUserChat: (id: User.Id) -> Unit,
        val onConnectedFlow: Flow<Unit>,
        val lastListViewModelFlow: Flow<List<ChatMessageContainer>>,
        val unreadCountsFlow: (List<ChatMessage>) -> Flow<Map<ChatMessage.Id, Long>>,
        val getUsersByIdsFlow: (List<User.Id>) -> Flow<List<User>>,
        val lastPresentMessageIdFlow: Flow<ChatMessage.Id>,
        val lastReadPresenceFlow: Flow<List<LastReadMessage>>,
        val sendFrontWebSocketMsg: suspend (WebSocketMsg.Front) -> Unit,
    )

    private val chatListItemsFlow = services
        .lastListViewModelFlow
        .flatMapLatest { chatMessagesWithStatus ->
            val unreadCountsFlow =
                services.unreadCountsFlow(chatMessagesWithStatus.map { it.message })
            services.getUsersByIdsFlow(chatMessagesWithStatus.map { it.message.secondId(currentUid) })
                .combine(unreadCountsFlow) { users, unreadCounts ->
                    chatMessagesWithStatus.mapNotNull { messageContainer ->
                        ChatListFeature.State.ListItem(
                            user = users.firstOrNull {
                                it.id == messageContainer.message.secondId(currentUid)
                            } ?: return@mapNotNull null,
                            lastMessage = messageContainer.viewModel,
                            unreadCount = unreadCounts[messageContainer.message.id]?.toInt()
                                ?: 0,
                        )
                    }
                }
        }

    init {
        chatListItemsFlow
            .collectIn(effHandlerScope) { chatList ->
                listener(Msg.UpdateItems(chatList))
            }
        services.lastPresentMessageIdFlow
            .combineWithUnit(services.onConnectedFlow)
            .debounce(100)
            .collectIn(effHandlerScope) { lastPresentMessageId ->
                Napier.i("WebSocketMsg.Front.SetChatMessagePresence $lastPresentMessageId")
                services.sendFrontWebSocketMsg(
                    WebSocketMsg.Front.SetChatMessagePresence(
                        messagePresenceId = lastPresentMessageId
                    )
                )
            }
        services.lastReadPresenceFlow
            .combineWithUnit(services.onConnectedFlow)
            .debounce(100)
            .collectIn(effHandlerScope) { lastReadPresence ->
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

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            is Eff.SelectChat -> {
                services.openUserChat(eff.uid)
            }
        }
    }
}