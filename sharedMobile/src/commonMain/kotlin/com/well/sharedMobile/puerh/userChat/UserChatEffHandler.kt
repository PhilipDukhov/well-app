package com.well.sharedMobile.puerh.userChat

import com.well.modules.db.chatMessages.ChatMessages
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.insertTmpMessage
import com.well.modules.db.chatMessages.toChatMessage
import com.well.modules.db.users.UsersDatabase
import com.well.modules.db.users.getByIdFlow
import com.well.modules.flowHelper.mapIterable
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.ContextHelper
import com.well.sharedMobile.puerh.userChat.UserChatFeature.Eff
import com.well.sharedMobile.puerh.userChat.UserChatFeature.Msg
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.toChatMessageWithStatusFlow
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class UserChatEffHandler(
    private val currentUid: UserId,
    private val peerUid: UserId,
    private val networkManager: NetworkManager,
    usersDatabase: UsersDatabase,
    private val messagesDatabase: ChatMessagesDatabase,
    private val contextHelper: ContextHelper,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    private val userFlow = usersDatabase.usersQueries.getByIdFlow(uid = peerUid)
    private val messagesFlow = messagesDatabase.chatMessagesQueries
        .chatList(firstId = currentUid, secondId = peerUid)
        .asFlow()
        .mapToList()
        .mapIterable(ChatMessages::toChatMessage)
        .toChatMessageWithStatusFlow(currentUid = currentUid, messagesDatabase = messagesDatabase)

    init {
        coroutineScope.launch {
            userFlow.collect {
                listener?.invoke(Msg.UpdateUser(it))
            }
        }
        coroutineScope.launch {
            messagesFlow.collect {
                listener?.invoke(Msg.UpdateMessages(it))
                println("Msg.UpdateMessages $it")
            }
        }
    }

    override fun handleEffect(eff: Eff) {
        when (eff) {
            Eff.Back,
            is Eff.Call,
            -> Unit
            Eff.ChooseImage -> {
                println("Eff.ChooseImage")
                coroutineScope.launch {
                    println("Eff.ChooseImage2")
                    listener?.invoke(Msg.SendImage(contextHelper.pickSystemImage()))
                }
            }
            is Eff.MarkMessageRead -> TODO()
            is Eff.SendImage -> {
                println("Eff.SendImage")
                val imageContainer = eff.image.toImageContainer()
                val newMessage: ChatMessage = messagesDatabase
                    .insertTmpMessage(
                        fromId = currentUid,
                        peerId = peerUid,
                        content = ChatMessage.Content.Image(eff.image.path, aspectRatio = imageContainer.size.aspectRatio),
                    )
                println("Eff.SendImage $newMessage")
                coroutineScope.launch {
                    val photoUrl = networkManager.uploadMessagePicture(imageContainer)
                    println("Eff.SendImage uploadMessagePicture $photoUrl")
                    contextHelper.appContext.cacheImage(
                        image = imageContainer,
                        url = photoUrl,
                    )
                    networkManager.send(
                        WebSocketMsg.Front.CreateChatMessage(
                            message = newMessage.copy(
                                content = ChatMessage.Content.Image(photoUrl),
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
                println("Eff.SendMessage $newMessage")
                coroutineScope.launch {
                    networkManager.send(WebSocketMsg.Front.CreateChatMessage(message = newMessage))
                }
            }
        }
    }
}
