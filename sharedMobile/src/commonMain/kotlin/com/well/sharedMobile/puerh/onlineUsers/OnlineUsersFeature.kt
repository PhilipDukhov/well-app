package com.well.sharedMobile.puerh.onlineUsers

import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.networking.NetworkManager.Status.Disconnected
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet

object OnlineUsersFeature {
    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    data class State(
        val users: List<User>,
        val connectionStatus: NetworkManager.Status,
        val currentUser: User? = null,
        val filter: UsersFilter = UsersFilter(),
    )

    sealed class Msg {
        data class OnConnectionStatusChange(val connectionStatus: NetworkManager.Status) : Msg()
        data class OnUsersUpdated(val users: List<User>) : Msg()
        data class OnUserSelected(val user: User) : Msg()
        data class OnUserFavorite(val user: User) : Msg()
        object OnCurrentUserSelected : Msg()
        object OnLogout : Msg()
        data class OnCurrentUserUpdated(val user: User?) : Msg()

        data class SetFilter(val filter: UsersFilter) : Msg()
        data class SetSearchString(val searchString: String) : Msg()
        object ToggleFilterFavorite : Msg()
    }

    sealed class Eff {
        data class SelectedUser(val user: User) : Eff()
        data class CallUser(val user: User) : Eff()
        object Logout : Eff()
        data class UpdateList(val filter: UsersFilter) : Eff()
        data class SetUserFavorite(val setter: FavoriteSetter) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.OnConnectionStatusChange -> {
                    return@reducer state.copy(
                        connectionStatus = msg.connectionStatus,
                    ) toSetOf Eff.UpdateList(state.filter)
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
                is Msg.OnUserFavorite -> {
                    val newUser = msg.user.copy(favorite = !msg.user.favorite)
                    return@reducer state.copy(
                        users = state.users.run {
                            val result = this.toMutableList()
                            val index = result.indexOfFirst { it.id == newUser.id }
                            result[index] = newUser
                            result
                        }
                    ) toSetOf Eff.SetUserFavorite(FavoriteSetter(newUser.favorite, newUser.id))
                }
                Msg.OnLogout -> {
                    return@eff Eff.Logout
                }
                is Msg.SetFilter -> {
                    return@reducer state.updateFilter(msg.filter)
                }
                is Msg.SetSearchString -> {
                    return@reducer state.updateFilter(
                        state.filter.copy(searchString = msg.searchString)
                    )
                }
                is Msg.ToggleFilterFavorite ->  {
                    return@reducer state.updateFilter(
                        state.filter.copy(favorite = !state.filter.favorite)
                    )
                }
            }
        })
    }.withEmptySet()

    private fun State.updateFilter(filter: UsersFilter) =
        if (this.filter != filter) {
            copy(
                filter = filter,
                users = listOf(),
            ) toSetOf Eff.UpdateList(filter)
        } else {
            this.withEmptySet()
        }
}