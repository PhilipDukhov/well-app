package com.well.modules.features.userChat.userChatHandlers

import com.well.modules.features.userChat.userChatFeature.UserChatFeature.Eff
import com.well.modules.features.userChat.userChatFeature.UserChatFeature.Msg
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageContainer
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.mapIterable
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserChatEffHandler(
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    data class Services(
        val peerUserFlow: Flow<User>,
        val messagesFlow: Flow<List<ChatMessageContainer>>,
        val pickSystemImage: suspend () -> LocalImage,
        val markRead: (ChatMessage.Id) -> Unit,
        val sendImage: (LocalImage) -> Unit,
        val sendText: (String) -> Unit,
    )

    init {
        services
            .peerUserFlow
            .map(Msg::UpdateUser)
            .collectIn(coroutineScope, ::listener)
        services
            .messagesFlow
            .mapIterable { it.viewModel }
            .map(Msg::UpdateMessages)
            .collectIn(coroutineScope, ::listener)
    }

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            Eff.Back,
            is Eff.Call,
            is Eff.OpenUserProfile,
            -> Unit
            Eff.ChooseImage -> {
                coroutineScope.launch {
                    listener(Msg.SendImage(services.pickSystemImage()))
                }
            }
            is Eff.MarkMessageRead -> {
                services.markRead(eff.messageId)
            }
            is Eff.SendImage -> {
                services.sendImage(eff.image)
            }
            is Eff.SendMessage -> {
                services.sendText(eff.string)
            }
        }
    }
}