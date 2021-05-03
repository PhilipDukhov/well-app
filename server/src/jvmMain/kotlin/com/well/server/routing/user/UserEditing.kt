package com.well.server.routing.user

import com.well.server.utils.Dependencies
import com.well.modules.models.User
import io.ktor.application.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import java.io.File

suspend fun PipelineContext<*, ApplicationCall>.updateUser(
    dependencies: Dependencies
) = dependencies.run {
    call.receive<User>().apply {
        database
            .usersQueries
            .updateUser(
                id = id,
                fullName = fullName,
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
        notifyUserUpdated(id)
    }
    call.respond(HttpStatusCode.OK, Unit)
}

suspend fun PipelineContext<*, ApplicationCall>.uploadUserProfile(
    dependencies: Dependencies
) = dependencies.run {
    call.receiveMultipart()
        .forEachPart { part ->
            if (part !is PartData.FileItem) throw IllegalStateException("unexpected part $part")
            val fileBytes = part.streamProvider().readBytes()
            val url = awsManager.upload(
                fileBytes,
                awsProfileImagePath(
                    part.name!!.toInt(),
                    File(part.originalFileName!!).extension
                )
            )
            call.respond(url.toString())
            part.dispose()
        }
}