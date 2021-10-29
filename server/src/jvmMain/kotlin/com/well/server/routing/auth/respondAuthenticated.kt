package com.well.server.routing.auth

import com.well.modules.models.AuthResponse
import com.well.modules.models.UserId
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondAuthenticated(
    uid: UserId,
    dependencies: Dependencies,
) = respond(
    HttpStatusCode.Created,
    AuthResponse(
        token = dependencies.jwtConfig.makeToken(uid),
        user = dependencies.getCurrentUser(uid)
    )
)
