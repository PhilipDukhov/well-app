package com.well.modules.features.userChat.userChatHandlers

import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.chatMessageWithStatusFlow
import com.well.modules.db.chatMessages.insertTmpMessage
import com.well.modules.features.userChat.userChatFeature.UserChatFeature.Eff
import com.well.modules.features.userChat.userChatFeature.UserChatFeature.Msg
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.chat.ChatMessage
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserChatEffHandler(
    private val currentUid: UserId,
    private val peerUid: UserId,
    private val services: Services,
    private val messagesDatabase: ChatMessagesDatabase,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    data class Services(
        val createChatMessage: suspend (ChatMessage) -> Unit,
        val uploadMessagePicture: suspend (ImageContainer) -> String,
        val pickSystemImage: suspend () -> LocalImage,
        val cacheImage: (ImageContainer, String) -> Unit,
        val peerUserFlow: () -> Flow<User>,
    )

    private val messagesFlow = messagesDatabase.chatMessageWithStatusFlow(
        currentUid = currentUid,
        peerUid = peerUid,
    )

    init {
        coroutineScope.launch {
            services.peerUserFlow().collect {
                listener(Msg.UpdateUser(it))
            }
        }
        coroutineScope.launch {
            messagesFlow.collect {
                listener(Msg.UpdateMessages(it))
                Napier.i("Msg.UpdateMessages $it")
            }
        }
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
                messagesDatabase.transaction {
                    messagesDatabase.lastReadMessagesQueries
                        .run {
                            val message = eff.message
                            val currentLast = selectSingle(
                                fromId = message.fromId,
                                peerId = message.peerId,
                            ).executeAsOneOrNull()
                            if (currentLast == null || currentLast.messageId < message.id) {
                                insert(fromId = message.fromId,
                                    peerId = message.peerId,
                                    messageId = message.id)
                                Napier.i("insert lastReadMessagesQueries $message")
                            }
                        }
                }
            }
            is Eff.SendImage -> {
                val imageContainer = eff.image.toImageContainer()
                val aspectRatio = imageContainer.size.aspectRatio
                val newMessage: ChatMessage = messagesDatabase
                    .insertTmpMessage(
                        fromId = currentUid,
                        peerId = peerUid,
                        content = ChatMessage.Content.Image(
                            eff.image.path,
                            aspectRatio = aspectRatio
                        ),
                    )
                coroutineScope.launch {
                    val photoUrl = services.uploadMessagePicture(imageContainer)
                    services.cacheImage(imageContainer, photoUrl)
                    services.createChatMessage(
                        newMessage.copy(
                            content = ChatMessage.Content.Image(
                                photoUrl,
                                aspectRatio = aspectRatio
                            )
                        )
                    )
                }
            }
            is Eff.SendMessage -> {
                val newMessage: ChatMessage = messagesDatabase
                    .insertTmpMessage(
                        fromId = currentUid,
                        peerId = peerUid,
                        content = ChatMessage.Content.Text(eff.string),
                    )
                Napier.i("Eff.SendMessage $newMessage")
                coroutineScope.launch {
                    services.createChatMessage(newMessage)
                }
            }
        }
    }
}