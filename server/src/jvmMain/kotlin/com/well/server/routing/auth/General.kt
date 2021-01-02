package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.serverModels.User
import com.well.serverModels.UserId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

suspend fun ApplicationCall.respondAuthenticated(
    userId: UserId,
    dependencies: Dependencies,
) = respond(
        HttpStatusCode.Created,
        mapOf(
            "token" to dependencies.jwtConfig.makeToken(userId),
        )
    )
