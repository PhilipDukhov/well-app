package com.well.sharedMobile.puerh.userChat

import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.sharedImage.LocalImage
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.ChatMessageWithStatus

object UserChatFeature {
    data class State(
        internal val peerId: UserId,
        val user: User? = null,
        val messages: List<ChatMessageWithStatus> = listOf(),
    )

    sealed class Msg {
        data class UpdateUser(val user: User) : Msg()
        data class UpdateMessages(val messages: List<ChatMessageWithStatus>) : Msg()
        data class MarkMessageRead(val message: ChatMessage) : Msg()
        data class SendMessage(val string: String) : Msg()
        object ChooseImage: Msg()
        data class SendImage(val image: LocalImage) : Msg()
        object Back : Msg()
        object Call : Msg()
    }

    sealed class Eff {
        object ChooseImage: Eff()
        data class MarkMessageRead(val message: ChatMessage) : Eff()
        data class SendMessage(val string: String, val peerId: UserId) : Eff()
        data class SendImage(val image: LocalImage, val peerId: UserId) : Eff()
        object Back : Eff()
        data class Call(val user: User) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.ChooseImage -> {
                    return@eff Eff.ChooseImage
                }
                is Msg.MarkMessageRead -> {
                    return@eff Eff.MarkMessageRead(msg.message)
                }
                is Msg.SendImage -> {
                    return@eff Eff.SendImage(image = msg.image, peerId = state.peerId)
                }
                is Msg.SendMessage -> {
                    val message = msg.string.trim()
                    if (message.isNotEmpty())
                        return@eff Eff.SendMessage(string = message, peerId = state.peerId)
                    else
                        return@state state
                }
                is Msg.UpdateMessages -> {
                    return@state state.copy(messages = msg.messages)
                }
                is Msg.UpdateUser -> {
                    return@state state.copy(user = msg.user)
                }
                Msg.Back -> return@eff Eff.Back
                Msg.Call -> return@eff Eff.Call(state.user!!)
            }
        })
    }.withEmptySet()
}