package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.toUser
import com.well.modules.models.User
import com.well.modules.models.UserId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun ApplicationCall.respondAuthenticated(
    userId: UserId,
    dependencies: Dependencies,
) = respond(
    HttpStatusCode.Created,
    mapOf(
        "token" to dependencies.jwtConfig.makeToken(userId),
        "user" to Json.encodeToString(
            dependencies.database
                .userQueries
                .getById(userId)
                .executeAsOne()
                .toUser()
        )
    )
)
