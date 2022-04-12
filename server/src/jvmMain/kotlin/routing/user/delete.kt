package com.well.server.routing.user

import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.delete(
    dependencies: Dependencies,
) = dependencies.run {
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
