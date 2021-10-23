package com.well.modules.features.experts

import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.modules.utils.map
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import com.well.modules.networking.NetworkManager
import com.well.modules.networking.NetworkManager.Status.Disconnected
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.modules.networking.NetworkManager.Status.Connected
import com.well.modules.features.call.CallFeature
import com.well.modules.features.call.drawing.DrawingFeature
import com.well.modules.features.experts.filter.FilterFeature

object ExpertsFeature {
    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    data class State(
        val users: List<User>,
        val connectionStatus: NetworkManager.Status,
        val currentUser: User? = null,
        val filterState: FilterFeature.State = FilterFeature.State(filter = UsersFilter()),
        val updatingFilter: Boolean = false,
    ) {
        val updating = connectionStatus != Connected || updatingFilter
    }

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
        object Reload : Msg()
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
                is Msg.Reload -> {
                    return@reducer state.reduceUpdateListIfConnected()
                }
                is Msg.OnConnectionStatusChange -> {
                    return@reducer state.copy(
                        connectionStatus = msg.connectionStatus,
                    ).reduceUpdateListIfConnected()
                }
                is Msg.OnUsersUpdated -> {
                    return@state state.copy(
                        users = msg.users,
                        updatingFilter = false,
                    )
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

    private fun State.reduceFilterMsg(msg: FilterFeature.Msg): Pair<State, Set<Eff>> =
        FilterFeature.reducer(msg, filterState).map(
            { newState ->
                copy(
                    filterState = newState,
                )
            },
            { effs ->
                effs.mapTo(HashSet<Eff>(), Eff::FilterEff)
            }
        ).let { result ->
            if (result.first.filterState.filter != filterState.filter) {
                result.reduceUpdateListIfConnected()
            } else {
                result
            }
        }

    private fun Pair<State, Set<Eff>>.reduceUpdateListIfConnected(): Pair<State, Set<Eff>> =
        first.reduceUpdateListIfConnected().let { result ->
            result.first to (second + result.second)
        }

    private fun State.reduceUpdateListIfConnected(): Pair<State, Set<Eff>> =
        copy(
            updatingFilter = false,
            users = emptyList(),
        ) toSetOf if (connectionStatus == Connected) {
            Eff.UpdateList(filterState.filter)
        } else null

}