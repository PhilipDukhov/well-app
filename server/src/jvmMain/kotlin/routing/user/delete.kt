package com.well.server.routing.user

import com.well.server.utils.Services
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.delete(
    services: Services,
) = services.run {
    val currentUid = call.authUid
    database.transaction {
        with(database) {
            availabilitiesQueries.clearUser(currentUid)
            chatMessagesQueries.clearUser(currentUid)
            favoritesQueries.clearUser(currentUid)
            lastReadMessagesQueries.clearUser(currentUid)
            meetingsQueries.clearUser(currentUid)
            notificationTokensQueries.clearUser(currentUid)
            ratingQueries.clearUser(currentUid)
            usersQueries.delete(currentUid)
        }
    }
    call.respond(HttpStatusCode.OK)
}
