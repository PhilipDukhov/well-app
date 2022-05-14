package com.well.server.routing.user

import com.well.modules.db.server.updateUser
import com.well.modules.models.User
import com.well.server.utils.Services
import com.well.server.utils.ForbiddenException
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.updateUser(
    services: Services,
) = services.run {
    val user = call.receive<User>()
    if (call.authUid != user.id) {
        throw ForbiddenException()
    }
    database
        .usersQueries
        .updateUser(user)
    call.respond(HttpStatusCode.OK)
}
