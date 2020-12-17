package com.well.shared.puerh.onlineUsers

import com.well.serverModels.User
import com.well.shared.puerh.WebSocketManager
import com.well.shared.puerh.WebSocketManager.Status.Disconnected

object OnlineUsersFeature {
    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    fun initialEffects(): Set<Eff> = setOf(Eff.ConnectToServer)

    data class State(
        val users: List<User>,
        val connectionStatus: WebSocketManager.Status,
    )

    sealed class Msg {
        data class OnConnectionStatusChange(val connectionStatus: WebSocketManager.Status) : Msg()
        data class OnUsersUpdated(val users: List<User>) : Msg()
        data class OnUserSelected(val user: User) : Msg()
    }

    sealed class Eff {
        object ConnectToServer : Eff()
        data class CallUser(val user: User) : Eff()
    }

    fun reducer(msg: Msg, state: State): Pair<State, Set<Eff>> = when (msg) {
        is Msg.OnConnectionStatusChange -> {
            state.copy(connectionStatus = msg.connectionStatus) to
                    if (msg.connectionStatus == Disconnected)
                        setOf(Eff.ConnectToServer)
                    else
                        emptySet()
        }
        is Msg.OnUsersUpdated ->  {
            println("tesststst3 ${msg.users}")
            state.copy(users = msg.users) to emptySet()
        }
        is Msg.OnUserSelected -> state to setOf(Eff.CallUser(msg.user))
    }
}