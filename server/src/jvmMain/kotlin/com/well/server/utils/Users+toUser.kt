package com.well.server.utils

import com.well.server.Users
import com.well.modules.models.User
import com.well.modules.models.User.Type
import com.well.modules.models.UserId
import com.well.server.Database

fun Users.toUser(
    currentUid: UserId,
    database: Database,
): User {
    val isCurrent = currentUid == id
    return User(
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