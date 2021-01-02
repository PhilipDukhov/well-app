package com.well.sharedMobile.puerh.call

import com.well.serverModels.User
import com.well.serverModels.UserId
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.puerh.call.CallFeature.State.Status.*

object CallFeature {
    fun callInitiateStateAndEffects(user: User) =
        State(user, Calling) to setOf(Eff.Initiate(user.id))

    fun incomingInitialState(incomingCall: WebSocketMessage.IncomingCall) =
        State(incomingCall.user, Incoming, incomingCall)

    data class State(
        val user: User,
        val status: Status,
        val incomingCall: WebSocketMessage.IncomingCall? = null,
        val localVideoContext: VideoViewContext? = null,
        val remoteVideoContext: VideoViewContext? = null,
    ) {
        enum class Status {
            Calling,
            Incoming,
            Connecting,
            Ongoing,
            ;

            val stringRepresentation: String
                get() = when (this) {
                    Calling -> "Calling"
                    Incoming -> "Incoming"
                    Connecting -> "Connecting"
                    Ongoing -> "Ongoing"
                }
        }
    }

    sealed class Msg {
        object Accept : Msg()
        object End : Msg()
        data class UpdateStatus(val status: State.Status) : Msg()
        data class UpdateLocalVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteVideoContext(val viewContext: VideoViewContext) : Msg()
    }

    sealed class Eff {
        data class Initiate(val userId: UserId) : Eff()
        data class Accept(val incomingCall: WebSocketMessage.IncomingCall) : Eff()
        object End : Eff()
    }

    fun reducer(msg: Msg, state: State): Pair<State, Set<Eff>> = when (msg) {
        Msg.Accept -> state.incomingCall?.let { incomingCall ->
            state.copy(status = Connecting) to setOf(Eff.Accept(incomingCall))
        } ?: throw IllegalStateException("$msg | $state")
        Msg.End -> state to setOf(Eff.End)
        is Msg.UpdateLocalVideoContext -> state.copy(localVideoContext = msg.viewContext) to setOf()
        is Msg.UpdateRemoteVideoContext -> state.copy(remoteVideoContext = msg.viewContext) to setOf()
        is Msg.UpdateStatus -> state.copy(status = msg.status) to setOf()
    }
}