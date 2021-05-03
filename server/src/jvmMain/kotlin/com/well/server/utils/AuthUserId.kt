package com.well.server.utils

import com.auth0.jwt.interfaces.Payload
import com.well.modules.models.UserId
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*

val ApplicationCall.authUserId: UserId
    get() = principal<JWTPrincipal>()!!.payload.authUserId!!

fun Payload.createPrincipal(dependencies: Dependencies) : JWTPrincipal? =
    authUserId?.let {
        val userExists = dependencies.database
            .usersQueries
            .exists(it)
            .executeAsOne()
        if (userExists) JWTPrincipal(this) else null
    }

private val Payload.authUserId: UserId?
    get() = claims[JwtConfig.userIdKey]?.asInt()
