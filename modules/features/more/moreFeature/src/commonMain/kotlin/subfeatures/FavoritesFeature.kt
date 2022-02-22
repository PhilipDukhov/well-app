package com.well.modules.features.more.moreFeature.subfeatures

import com.well.modules.features.more.moreFeature.MoreFeature
import com.well.modules.models.User
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet

object FavoritesFeature {
    data class State(
        internal val allUsers: List<User> = emptyList(),
        val filterString: String? = null,
    ) {
        val users = if (filterString == null)
            allUsers
        else {
            val filterWords = filterString.split(" ")
            allUsers.filter {
                filterWords.all(it.fullName.lowercase()::contains)
            }
        }
        val title = MoreFeature.State.Item.Favorites.title
    }

    sealed class Msg {
        data class OnUserSelected(val user: User) : Msg()
        data class UpdateUsers(val users: List<User>) : Msg()
        data class UpdateFilterString(val filterString: String?) : Msg()
        data class OnUserFavorite(val user: User) : Msg()
        object Back : Msg()
    }

    sealed interface Eff {
        data class UnFavoriteUser(val uid: User.Id) : Eff
        data class SelectedUser(val uid: User.Id) : Eff
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Back -> {
                    return@eff Eff.Back
                }
                is Msg.OnUserFavorite -> {
                    return@eff Eff.UnFavoriteUser(msg.user.id)
                }
                is Msg.OnUserSelected -> {
                    return@eff Eff.SelectedUser(msg.user.id)
                }
                is Msg.UpdateUsers -> {
                    return@state state.copy(allUsers = msg.users)
                }
                is Msg.UpdateFilterString -> {
                    return@state state.copy(filterString = msg.filterString)
                }
            }
        })
    }.withEmptySet()
}