package com.well.server.utils

import com.well.server.Users
import com.well.modules.models.User
import com.well.modules.models.UserId

fun Users.toUser(favorite: Boolean = false): User =
    User(
        id = id,
        initialized = initialized,
        favorite = favorite,
        fullName = fullName,
        type = type,
        email = email,
        profileImageUrl = profileImageUrl,
        phoneNumber = phoneNumber,
        location = location,
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