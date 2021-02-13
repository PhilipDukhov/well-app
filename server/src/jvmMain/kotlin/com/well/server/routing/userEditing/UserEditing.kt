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
    val multipart = call.receiveMultipart()
    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                println("FormItem ${part.name}")
                if (part.name == "title") {
//                    title = part.value
                }
            }
            is PartData.FileItem -> {
                val ext = File(part.originalFileName!!).extension

                val file = File(
                    System.getProperty("java.io.tmpdir"),
                    "upload-${System.currentTimeMillis()}.$ext"
                )
                part.streamProvider()
                    .use { input ->
                        file.outputStream()
                            .buffered()
                            .use { output -> input.copyToSuspend(output) }
                    }

//                awsManager
//                    .upload(
//                        file.readBytes(),
//                        awsProfileImagePath()
//                    )
            }
            is PartData.BinaryItem -> {
                println("BinaryItem $part")
            }
        }

        part.dispose()
    }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            println("read $bytes")
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
