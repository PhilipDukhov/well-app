package com.well.modules.features.userChat.userChatFeature

import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageViewModel
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.GlobalStringsBase
import com.well.modules.utils.viewUtils.sharedImage.LocalImage

object UserChatFeature {
    object Strings: GlobalStringsBase() {
        const val meetingScheduled = "Meeting scheduled"

        fun meetingStars(meeting: Meeting) =
            "on ${meeting.startDay.localizedDayAndShortMonth()} at ${meeting.startTime}"
    }

    data class State(
        val peerId: User.Id,
        val user: User? = null,
        val backToUser: Boolean,
        val messages: List<ChatMessageViewModel> = listOf(),
    )

    sealed class Msg {
        data class UpdateUser(val user: User) : Msg()
        data class UpdateMessages(val messages: List<ChatMessageViewModel>) : Msg()
        data class MarkMessageRead(val messageId: ChatMessage.Id) : Msg()
        data class SendMessage(val string: String) : Msg()
        object ChooseImage : Msg()
        data class SendImage(val image: LocalImage) : Msg()
        object OpenUserProfile : Msg()
        object Back : Msg()
        object Call : Msg()
    }

    sealed interface Eff {
        object ChooseImage : Eff
        data class MarkMessageRead(val messageId: ChatMessage.Id) : Eff
        data class SendMessage(val string: String, val peerId: User.Id) : Eff
        data class SendImage(val image: LocalImage, val peerId: User.Id) : Eff
        object Back : Eff
        data class Call(val user: User) : Eff
        data class OpenUserProfile(val user: User) : Eff
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
                    return@eff Eff.MarkMessageRead(msg.messageId)
                }
                is Msg.SendImage -> {
                    return@eff Eff.SendImage(image = msg.image, peerId = state.peerId)
                }
                is Msg.SendMessage -> {
                    if (msg.string.isNotBlank())
                        return@eff Eff.SendMessage(
                            string = msg.string.trim(),
                            peerId = state.peerId
                        )
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
                Msg.OpenUserProfile -> {
                    if (state.backToUser)
                        return@eff Eff.Back
                    else
                        return@eff Eff.OpenUserProfile(state.user!!)
                }
            }
        })
    }.withEmptySet()
}