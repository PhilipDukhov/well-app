package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.db.chatMessages.chatMessagesFlow
import com.well.modules.db.chatMessages.insertTmpMessage
import com.well.modules.db.chatMessages.markRead
import com.well.modules.db.users.getByIdFlow
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.features.userChat.userChatHandlers.UserChatEffHandler
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.kotlinUtils.launchedIn
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import com.well.modules.utils.viewUtils.sharedImage.asByteArrayOptimizedForNetwork
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope

internal class TopUserChatEffHandler(
    private val peerUid: User.Id,
    private val currentUid: User.Id,
    private val featureProviderImpl: TopLevelFeatureProviderImpl,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<UserChatFeature.Eff, UserChatFeature.Msg>(parentCoroutineScope) {
    private val handler = with(featureProviderImpl) {
        UserChatEffHandler(
            services = UserChatEffHandler.Services(
                peerUserFlow = usersQueries.getByIdFlow(peerUid),
                messagesFlow = messagesDatabase
                    .chatMessagesFlow(
                        currentUid = currentUid,
                        peerUid = peerUid,
                    )
                    .toChatMessageContainerFlow(currentUid, featureProviderImpl),
                pickSystemImage = systemHelper!!::pickSystemImage,
                markRead = messagesDatabase::markRead,
                sendImage = ::sendImage.launchedIn(parentCoroutineScope),
                sendText = ::sendText.launchedIn(parentCoroutineScope),
            ),
            parentCoroutineScope = parentCoroutineScope,
        )
    }

    override fun listener(msg: UserChatFeature.Msg) {
        super.listener(msg)
        Napier.d("listener $msg")
    }

    init {
        handler.setListener(::listener)
        addCloseableChild(handler)
    }

    override suspend fun processEffect(eff: UserChatFeature.Eff) {
        handler.handleEffect(eff)
    }

    private suspend fun sendImage(localImage: LocalImage) {
        val imageContainer = localImage.toImageContainer()
        val aspectRatio = imageContainer.size.aspectRatio
        val newMessage: ChatMessage =
            featureProviderImpl.messagesDatabase.insertTmpMessage(
                fromId = currentUid,
                peerId = peerUid,
                ChatMessage.Content.Image(
                    localImage.path,
                    aspectRatio = aspectRatio
                )
            )
        val photoUrl = featureProviderImpl.networkManager.uploadMessagePicture(
            imageContainer.asByteArrayOptimizedForNetwork()
        )
        featureProviderImpl.systemHelper?.systemContext
            ?.cacheImage(imageContainer, photoUrl)
        featureProviderImpl.networkManager.sendFront(
            WebSocketMsg.Front.CreateChatMessage(
                newMessage.copy(
                    content = ChatMessage.Content.Image(
                        photoUrl,
                        aspectRatio = aspectRatio
                    )
                )
            )
        )
    }

    private suspend fun sendText(text: String) {
        val newMessage: ChatMessage =
            featureProviderImpl.messagesDatabase.insertTmpMessage(
                fromId = currentUid,
                peerId = peerUid,
                ChatMessage.Content.Text(text),
            )
        featureProviderImpl.networkManager.sendFront(
            WebSocketMsg.Front.CreateChatMessage(
                newMessage
            )
        )
    }
}