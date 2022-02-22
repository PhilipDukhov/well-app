package com.well.modules.features.experts.expertsFeature.filter

import com.well.modules.models.UsersFilter
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.GlobalStringsBase
import com.well.modules.utils.viewUtils.UIEditingField

object FilterFeature {
    object Strings : GlobalStringsBase() {
        const val activity = "Activity"
        const val sortBy = "Sort by"
        const val withReviews = "With reviews"
        const val clearAll = "clear all"
        const val show = "Show"
        const val rating = "Rating"
    }

    data class State(
        val filter: UsersFilter,
    ) {
        val fields = filter.fields()
        val sortByIndex = UsersFilter.SortBy.values().indexOf(filter.sortBy)
        val ratingIndex = UsersFilter.Rating.values().indexOf(filter.rating)

        @Suppress("UNCHECKED_CAST")
        private fun UsersFilter.fields() = listOf(
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.skillsExpertise,
                selection = filter.skills,
            ) { Msg.Update(copy(skills = it.toSet())) },
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.academicRank,
                selection = filter.academicRanks,
            ) { Msg.Update(copy(academicRanks = it.toSet())) },
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.languagesSpoken,
                selection = filter.languages,
            ) { Msg.Update(copy(languages = it.toSet())) },
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.activity,
                selection = filter.activity,
            ) { Msg.Update(copy(activity = it.toSet())) },
            UIEditingField(
                placeholder = Strings.country,
                content = UIEditingField.Content.List.countryCodesList(countryCode),
                updateMsg = {
                    Msg.Update(copy(countryCode = it.selectedItems.firstOrNull()))
                }
            ),
        ) as List<UIEditingField<UIEditingField.Content.List<*>, Msg.Update>>
    }

    sealed class Msg {
        data class Update(val filter: UsersFilter) : Msg()
        data class SetSortByIndex(val index: Int) : Msg()
        data class SetRatingIndex(val index: Int) : Msg()
        object ToggleWithReviews : Msg()
        object Clear : Msg()
        object Show : Msg()
    }

    sealed interface Eff {
        data class Show(val usersFilter: UsersFilter) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.Update -> return@state state.copy(filter = msg.filter)
                is Msg.Clear -> return@state state.copy(filter = UsersFilter.default())
                is Msg.Show -> return@eff Eff.Show(state.filter)
                is Msg.SetRatingIndex -> return@state state.copyFilter {
                    copy(rating = UsersFilter.Rating.values()[msg.index])
                }
                is Msg.SetSortByIndex -> return@state state.copyFilter {
                    copy(sortBy = UsersFilter.SortBy.values()[msg.index])
                }
                is Msg.ToggleWithReviews -> {
                    return@state state.copyFilter {
                        copy(withReviews = !withReviews)
                    }
                }
            }
        })
    }.withEmptySet()

    private fun State.copyFilter(block: UsersFilter.() -> UsersFilter): State =
        copy(filter = block(filter))
}