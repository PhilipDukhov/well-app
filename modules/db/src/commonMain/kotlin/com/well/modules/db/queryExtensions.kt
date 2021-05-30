package com.well.modules.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.well.modules.models.User
import com.well.modules.models.UserPresenceInfo
import kotlinx.coroutines.flow.map

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
        ratingInfo = user.ratingInfo,
    )

fun UsersQueries.usersPresenceFlow() =
    getAllIdsWithEdited()
        .asFlow()
        .mapToList()
        .map { list ->
            list.map { UserPresenceInfo(id = it.id, lastEdited = it.lastEdited) }
        }

fun Users.toUser() = User(
    id = id,
    initialized = initialized,
    lastEdited = lastEdited,
    favorite = favorite,
    fullName = fullName,
    type = type,
    email = email,
    ratingInfo = ratingInfo,
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