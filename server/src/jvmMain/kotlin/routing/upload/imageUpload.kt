package com.well.server.routing.upload

import com.well.server.utils.Services
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import java.io.File
import java.util.*

suspend fun PipelineContext<*, ApplicationCall>.uploadProfilePicture(
    services: Services,
) = uploadMultipartFile(
    services,
    pathGenerator = { ext ->
        services.awsProfileImagePath(this@uploadProfilePicture.call.authUid, ext)
    }
)

suspend fun PipelineContext<*, ApplicationCall>.uploadMessageMedia(
    services: Services,
) = uploadMultipartFile(
    services,
    pathGenerator = { ext ->
        "messagePictures/${UUID.randomUUID()}.$ext"
    }
)

private suspend fun PipelineContext<*, ApplicationCall>.uploadMultipartFile(
    services: Services,
    pathGenerator: (String) -> String,
) {
    call.receiveMultipart()
        .forEachPart { part ->
            if (part !is PartData.FileItem) throw IllegalStateException("unexpected part $part")
            val fileBytes = part.streamProvider().readBytes()
            val fileName = part.originalFileName!!
            part.dispose()
            val url = services.awsManager.run {
                var path: String
                val ext = File(fileName).extension
                do {
                    path = pathGenerator(ext)
                } while (exists(path))
                upload(fileBytes, path)
            }
            call.respond(url.toString())
        }
}