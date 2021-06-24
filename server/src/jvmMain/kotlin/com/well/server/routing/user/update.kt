package com.well.server.routing.user

import com.well.modules.models.User
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.updateUser(
    dependencies: Dependencies
) = dependencies.run {
    call.receive<User>().apply {
        if (call.authUid != id) {
            call.respond(HttpStatusCode.Forbidden)
            return@run
        }
        database
            .usersQueries
            .updateUser(
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
    call.respond(HttpStatusCode.OK)
}