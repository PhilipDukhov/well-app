package com.well.server.routing.userEditing

import com.well.server.utils.Dependencies
import com.well.server.utils.toUser
import com.well.serverModels.User
import io.ktor.application.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream

suspend fun PipelineContext<*, ApplicationCall>.updateUser(
    dependencies: Dependencies
) = dependencies.run {
    call.receive<User>().apply {
        println("updateUser $this")
        database
            .userQueries
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