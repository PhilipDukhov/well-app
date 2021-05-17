package com.well.sharedMobile.puerh.experts

import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.networking.NetworkManager.Status.Disconnected
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.networking.NetworkManager.Status.Connected
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import com.well.sharedMobile.puerh.experts.filter.FilterFeature

object ExpertsFeature {
    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    data class State(
        val users: List<User>,
        val connectionStatus: NetworkManager.Status,
        val currentUser: User? = null,
        val filterState: FilterFeature.State = FilterFeature.State(filter = UsersFilter())
    )

    sealed class Msg {
        data class OnConnectionStatusChange(val connectionStatus: NetworkManager.Status) : Msg()
        data class OnUsersUpdated(val users: List<User>) : Msg()
        data class OnUserSelected(val user: User) : Msg()
        data class OnUserFavorite(val user: User) : Msg()
        object OnCurrentUserSelected : Msg()
        data class OnCurrentUserUpdated(val user: User?) : Msg()

        data class FilterMsg(val msg: FilterFeature.Msg) : Msg()
        data class SetSearchString(val searchString: String) : Msg()
        object ToggleFilterFavorite : Msg()
    }

    sealed class Eff {
        data class SelectedUser(val user: User) : Eff()
        data class CallUser(val user: User) : Eff()
        data class UpdateList(val filter: UsersFilter) : Eff()
        data class SetUserFavorite(val setter: FavoriteSetter) : Eff()
        data class FilterEff(val eff: FilterFeature.Eff) : Eff()
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
                    ) toSetOf if (msg.connectionStatus == Connected) Eff.UpdateList(state.filterState.filter) else null
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
                is Msg.FilterMsg -> {
                    return@reducer state.reduceFilterMsg(msg.msg)
                }
                is Msg.SetSearchString -> {
                    return@reducer state.reduceFilterMsg(
                        FilterFeature.Msg.Update(
                            state.filterState.filter.copy(
                                searchString = msg.searchString
                            )
                        )
                    )
                }
                is Msg.ToggleFilterFavorite -> {
                    return@reducer state.reduceFilterMsg(
                        FilterFeature.Msg.Update(
                            state.filterState.filter.copy(
                                favorite = !state.filterState.filter.favorite
                            )
                        )
                    )
                }
            }
        })
    }.withEmptySet()

//    private fun <State, Eff, ChildState, ChildEff, ChildMsg> reduceMsg(msg: ChildMsg, copy: (ChildState) -> State) {
//       TODO: make general reducer
//    }

    private fun State.reduceFilterMsg(msg: FilterFeature.Msg): Pair<State, Set<Eff>> {
        val (newState, effs) = FilterFeature.reducer(msg, filterState)
        return copy(
            filterState = newState,
        ) to effs
            .mapTo(HashSet<Eff>(), Eff::FilterEff)
            .apply {
                if (connectionStatus == Connected && newState.filter != filterState.filter) {
                    add(Eff.UpdateList(newState.filter))
                }
            }
    }
}