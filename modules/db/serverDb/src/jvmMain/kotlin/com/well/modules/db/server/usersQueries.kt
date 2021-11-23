package com.well.modules.db.server

import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.modules.utils.dbUtils.adaptedIntersectionRegex
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun UsersQueries.filterFlow(
    uid: User.Id,
    maxLastOnlineDistance: Long,
    specificIdsRegexp: String,
    filter: UsersFilter,
) = filter(
    nameFilter = filter.searchString,
    favorites = filter.favorite.toDbLong(),
    uid = uid,
    maxLastOnlineDistance = maxLastOnlineDistance,
    specificIdsRegexp = specificIdsRegexp,
    skillsRegexp = filter.skills.adaptedIntersectionRegex(),
    academicRankRegexp = filter.academicRanks.adaptedIntersectionRegex(),
    languagesRegexp = filter.languages.adaptedIntersectionRegex(),
    countryCode = filter.countryCode ?: "",
    withReviews = filter.withReviews.toDbLong(),
    rating = filter.rating.toDoubleOrNull(),
)
    .asFlow()
    .mapToList()

fun UsersQueries.updateUser(user: User) = user.apply {
    updateUser(
        id = id,
        fullName = fullName,
        email = email,
        profileImageUrl = profileImageUrl,
        phoneNumber = phoneNumber,
        countryCode = countryCode,
        timeZoneIdentifier = timeZoneIdentifier,
        credentials = credentials,
        academicRank = academicRank,
        languages = languages,
        skills = skills,
        bio = bio,
        education = education,
        professionalMemberships = professionalMemberships,
        publications = publications,
        twitter = twitter,
        doximity = doximity,
    )
}

fun UsersQueries.getByIdsFlow(ids: Set<User.Id>) =
    getByIds(ids)
        .asFlow()
        .mapToList()

private fun Boolean.toDbLong(): Long = if (this) 1 else 0

private fun UsersFilter.Rating.toDoubleOrNull(): Double? =
    when (this) {
        UsersFilter.Rating.All -> null
        UsersFilter.Rating.Five -> 5.0
        UsersFilter.Rating.Four -> 4.0
        UsersFilter.Rating.Three -> 3.0
        UsersFilter.Rating.Two -> 2.0
        UsersFilter.Rating.One -> 1.0
    }