package com.well.server.routing.auth

import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.twitterLogin(
    dependencies: Dependencies
) = dependencies.run {
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
    call.respondAuthenticated(id, dependencies)
}

private fun Dependencies.createTwitterUser(
    id: String,
): UserId = database
    .usersQueries
    .run {
        insertTwitter(
            type = User.Type.Doctor,
            twitterId = id,
        )
        lastInsertId()
            .executeAsOne()
            .toInt()
    }

private val OAuthAccessTokenResponse.extraParameters: Parameters
    get() = when (this) {
        is OAuthAccessTokenResponse.OAuth1a -> extraParameters
        is OAuthAccessTokenResponse.OAuth2 -> extraParameters
    }