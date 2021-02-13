package com.well.server.utils

import com.well.server.Users
import com.well.serverModels.User

fun Users.toUser(): User =
    User(
        id = id,
        initialized = initialized,
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