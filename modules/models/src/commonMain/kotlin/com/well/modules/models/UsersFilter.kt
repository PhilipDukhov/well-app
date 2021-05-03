package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class UsersFilter(
    val searchString: String = "",
    val favorite: Boolean = false,
    val sortBy: SortBy = SortBy.Popularity,
    val skills: Set<User.Skill> = emptySet(),
    val academicRanks: Set<User.AcademicRank> = emptySet(),
    val languages: Set<User.Language> = emptySet(),
    val location: String? = null,
    val rating: Rating = Rating.All,
    val withReviews: Boolean = true,
) {
    enum class SortBy {
        Popularity,
        Recent,
        ;
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
    }
}