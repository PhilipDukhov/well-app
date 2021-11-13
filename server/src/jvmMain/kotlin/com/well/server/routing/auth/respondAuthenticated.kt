package com.well.server.routing.auth

import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondAuthenticated(
    uid: User.Id,
    dependencies: Dependencies,
) = respond(
    HttpStatusCode.Created,
    AuthResponse(
        token = dependencies.jwtConfig.makeToken(uid),
        user = dependencies.getCurrentUser(uid)
    )
)
