package com.well.modules.features.experts.expertsFeature

import com.well.modules.features.experts.expertsFeature.filter.FilterFeature
import com.well.modules.models.ConnectionStatus
import com.well.modules.models.ConnectionStatus.Connected
import com.well.modules.models.ConnectionStatus.Disconnected
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.GlobalStringsBase

object ExpertsFeature {
    object Strings: GlobalStringsBase()

    fun initialState(): State = State(
        listOf(),
        Disconnected,
    )

    data class State(
        val users: List<User>,
        internal val connectionStatus: ConnectionStatus,
        val filterState: FilterFeature.State = FilterFeature.State(filter = UsersFilter()),
        val updatingFilter: Boolean = false,
    ) {
        val updating = connectionStatus != Connected || updatingFilter
        val connectionStatusDescription = connectionStatus.name
    }

    sealed class Msg {
        class OnConnectionStatusChange(val connectionStatus: ConnectionStatus) : Msg()
        class OnUsersUpdated(val users: List<User>) : Msg()
        class OnUserSelected(val user: User) : Msg()
        class OnUserFavorite(val user: User) : Msg()

        class FilterMsg(val msg: FilterFeature.Msg) : Msg()
        class SetSearchString(val searchString: String) : Msg()
        object ToggleFilterFavorite : Msg()
        object Reload : Msg()
    }

    sealed interface Eff {
        class SelectedUser(val user: User) : Eff
        class UpdateList(val filter: UsersFilter) : Eff
        class SetUserFavorite(val setter: FavoriteSetter) : Eff
        class FilterEff(val eff: FilterFeature.Eff) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
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
                is Msg.OnUserFavorite -> {
                    return@eff Eff.SetUserFavorite(FavoriteSetter(!msg.user.favorite, msg.user.id))
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