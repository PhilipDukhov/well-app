package com.well.sharedMobile.puerh.onlineUsers

import com.well.serverModels.User
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.networking.NetworkManager.Status.Disconnected
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
        data class OnCallUser(val user: User) : Msg()
        object OnCurrentUserSelected : Msg()
        object OnLogout : Msg()
        data class OnCurrentUserUpdated(val user: User?) : Msg()
    }

    sealed class Eff {
        data class SelectedUser(val user: User) : Eff()
        data class CallUser(val user: User) : Eff()
        object Logout : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.OnConnectionStatusChange -> {
                    return@state state.copy(connectionStatus = msg.connectionStatus)
                }
                is Msg.OnUsersUpdated -> {
                    return@state state.copy(users = msg.users)
                }
                is Msg.OnUserSelected -> {
                    return@eff Eff.SelectedUser(msg.user)
                }
                is Msg.OnCurrentUserUpdated -> {
                    return@state state.copy(currentUser = msg.user)
                }
                Msg.OnCurrentUserSelected -> {
                    return@eff Eff.SelectedUser(state.currentUser!!)
                }
                is Msg.OnCallUser -> {
                    return@eff Eff.CallUser(msg.user)
                }
                Msg.OnLogout -> {
                    return@eff Eff.Logout
                }
            }
        })
    }.withEmptySet()
}