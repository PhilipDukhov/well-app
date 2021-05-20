package com.well.server.utils

import com.well.modules.models.Rating
import com.well.server.Users
import com.well.modules.models.User
import com.well.modules.models.User.Type
import com.well.modules.models.UserId
import com.well.server.Database
import com.well.server.Ratings

fun Users.toUser(
    currentUid: UserId,
    database: Database,
) = (currentUid == id).let { isCurrent ->
    User(
        id = id,
        initialized = initialized,
        favorite = if (isCurrent) false else
            database.favoritesQueries.isFavorite(currentUid, id).executeAsOne(),
        fullName = fullName,
        type = if (isCurrent) type else when (type) {
            Type.Doctor,
            Type.Expert,
            -> type
            Type.PendingExpert,
            Type.DeclinedExpert,
            -> Type.Doctor
        },
        email = email,
        ratingInfo = User.RatingInfo(
            count = ratingsCount,
            average = averageRating,
            currentUserRating = if (isCurrent) null else
                database.ratingQueries.get(
                    owner = currentUid,
                    destination = id,
                ).executeAsOneOrNull()?.toRating()
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

fun Ratings.toRating(): Rating = Rating(value = value, text = text)
