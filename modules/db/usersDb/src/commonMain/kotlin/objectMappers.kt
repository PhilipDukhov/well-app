package com.well.modules.db.users

import com.well.modules.models.User

fun UsersQueries.insertOrReplace(user: User) =
    insertOrReplace(
        id = user.id,
        initialized = user.initialized,
        lastEdited = user.lastEdited,
        fullName = user.fullName,
        type = user.type,
        favorite = user.favorite,
        email = user.email,
        profileImageUrl = user.profileImageUrl,
        phoneNumber = user.phoneNumber,
        timeZoneIdentifier = user.timeZoneIdentifier,
        credentials = user.credentials,
        academicRank = user.academicRank,
        languages = user.languages,
        skills = user.skills,
        bio = user.bio,
        education = user.education,
        professionalMemberships = user.professionalMemberships,
        publications = user.publications,
        twitter = user.twitter,
        doximity = user.doximity,
        countryCode = user.countryCode,
        reviewInfo = user.reviewInfo,
    )

fun Users.toUser() = User(
    id = id,
    initialized = initialized,
    lastEdited = lastEdited,
    favorite = favorite,
    fullName = fullName,
    type = type,
    email = email,
    reviewInfo = reviewInfo,
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