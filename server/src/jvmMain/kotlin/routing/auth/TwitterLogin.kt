package com.well.server.routing.auth

import com.well.modules.models.User
import com.well.server.utils.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.twitterLogin(
    services: Services
) = services.run {
    val principal = call.authentication.principal<OAuthAccessTokenResponse>()
        ?: throw IllegalStateException("principal missing in twitter response: ${call.response}")
    val error = principal.extraParameters["error"]
    if (error != null) {
        call.respond(HttpStatusCode.NotFound, error)
        return
    }
    val twitterUid = principal.extraParameters["user_id"]
        ?: throw IllegalStateException("user_id missing in twitter response: ${principal.extraParameters}")
    val id = database
        .usersQueries
        .getByTwitterId(twitterUid)
        .executeAsOneOrNull()
        ?: run {
            createTwitterUser(twitterUid)
        }
    call.respondAuthenticated(id, services)
}

private fun Services.createTwitterUser(
    id: String,
): User.Id = database
    .usersQueries
    .run {
        insertTwitter(
            type = User.Type.Doctor,
            twitterId = id,
        )
        User.Id(
            lastInsertId()
                .executeAsOne()
        )
    }

private val OAuthAccessTokenResponse.extraParameters: Parameters
    get() = when (this) {
        is OAuthAccessTokenResponse.OAuth1a -> extraParameters
        is OAuthAccessTokenResponse.OAuth2 -> extraParameters
    }