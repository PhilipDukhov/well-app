package com.well.server.routing.user

import com.well.modules.models.User
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.requestBecomeExpert(
    dependencies: Dependencies
) = dependencies.run {
    val uid = call.authUid
    when (
        val type = database
            .usersQueries
            .getTypeById(uid)
            .executeAsOne()
    ) {
        User.Type.Doctor -> {
            database
                .usersQueries
                .updateType(
                    id = uid,
                    type = User.Type.PendingExpert
                )
            call.respond(HttpStatusCode.OK, Unit)
        }
        else -> {
            println("requestBecomeExpert Conflict $type")
            call.respond(HttpStatusCode.Conflict)
        }
    }
}