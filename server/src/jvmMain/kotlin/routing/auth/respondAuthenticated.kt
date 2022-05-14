package com.well.server.routing.auth

import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.server.utils.Services
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondAuthenticated(
    uid: User.Id,
    services: Services,
) = respond(
    HttpStatusCode.Created,
    AuthResponse(
        token = services.jwtConfig.makeToken(uid),
        user = services.getCurrentUser(uid)
    )
)
