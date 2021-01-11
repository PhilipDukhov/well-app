package com.well.sharedMobile.puerh.onlineUsers

import com.well.serverModels.User
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.sharedMobile.networking.webSocketManager.NetworkManager.Status.Disconnected
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object OnlineUsersFeature {
    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    data class State(
        val users: List<User>,
        val connectionStatus: NetworkManager.Status,
        val currentUser: User? = null,
    )

    sealed class Msg {
        data class OnConnectionStatusChange(val connectionStatus: NetworkManager.Status) : Msg()
        data class OnUsersUpdated(val users: List<User>) : Msg()
        data class OnUserSelected(val user: User) : Msg()
        data class OnCurrentUserUpdated(val user: User?) : Msg()
    }

    sealed class Eff {
        data class CallUser(val user: User) : Eff()
    }

    fun reducer(msg: Msg, state: State): Pair<State, Set<Eff>> = when (msg) {
        is Msg.OnConnectionStatusChange -> {
            state.copy(connectionStatus = msg.connectionStatus).withEmptySet()
        }
        is Msg.OnUsersUpdated ->  {
            state.copy(users = msg.users).withEmptySet()
        }
        is Msg.OnUserSelected -> {
            state toSetOf Eff.CallUser(msg.user)
        }
        is Msg.OnCurrentUserUpdated -> {
            state.copy(currentUser = msg.user).withEmptySet()
        }
    }
}