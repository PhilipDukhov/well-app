package com.well.modules.db.server

import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.modules.utils.dbUtils.adaptedIntersectionRegex
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
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

fun UsersQueries.getByIdsFlow(ids: Collection<User.Id>) =
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

fun Users.toUser(
    currentUid: User.Id,
    database: Database,
) = (currentUid == id).let { isCurrent ->
    User(
        id = id,
        initialized = initialized,
        lastEdited = lastEdited,
        favorite = if (isCurrent) false else
            database.favoritesQueries.isFavorite(currentUid, id).executeAsOne(),
        fullName = fullName,
        type = if (isCurrent) type else when (type) {
            User.Type.Doctor,
            User.Type.Expert,
            -> type
            User.Type.PendingExpert,
            User.Type.DeclinedExpert,
            -> User.Type.Doctor
        },
        email = email,
        ratingInfo = User.RatingInfo(
            count = ratingsCount,
            average = averageRating,
            currentUserRating = ifTrueOrNull(!isCurrent) {
                database.ratingQueries.get(
                    owner = currentUid,
                    destination = id,
                ).executeAsOneOrNull()?.toRating()
            }
        ),
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