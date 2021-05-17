package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class UsersFilter(
    val searchString: String = "",
    val favorite: Boolean = false,
    val sortBy: SortBy = SortBy.Popularity,
    val skills: Set<User.Skill> = emptySet(),
    val academicRanks: Set<User.AcademicRank> = emptySet(),
    val activity: Set<Activity> = emptySet(),
    val languages: Set<User.Language> = emptySet(),
    val countryCode: String? = null,
    val rating: Rating = Rating.All,
    val withReviews: Boolean = true,
) {
    enum class SortBy {
        Popularity,
        Recent,
        ;

        companion object {
            val allCases = values().toList()
        }
    }

    enum class Activity {
        LastWeek,
        Now,
        Tomorrow
        ;
    }

    enum class Rating {
        All,
        Five,
        Four,
        Three,
        Two,
        One,
        ;

        val title: String
            get() = when (this) {
                All -> "All"
                Five -> "5"
                Four -> "4"
                Three -> "3"
                Two -> "2"
                One -> "1"
            }

        companion object {
            val allCases = values().toList()
        }
    }

    companion object {
        fun default(searchString: String = "") = UsersFilter(searchString = searchString)
    }
}