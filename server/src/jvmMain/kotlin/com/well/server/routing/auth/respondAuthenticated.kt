package com.well.server.routing.auth

import com.well.modules.models.AuthResponse
import com.well.server.utils.Dependencies
import com.well.modules.models.UserId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun ApplicationCall.respondAuthenticated(
    userId: UserId,
    dependencies: Dependencies,
) = respond(
    HttpStatusCode.Created,
    AuthResponse(dependencies.jwtConfig.makeToken(userId), dependencies.getUser(userId))
)
