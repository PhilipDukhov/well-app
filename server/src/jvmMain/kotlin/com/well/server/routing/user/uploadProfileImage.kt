package com.well.server.routing.user

import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import java.io.File

suspend fun PipelineContext<*, ApplicationCall>.uploadProfileImage(
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
