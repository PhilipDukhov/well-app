package com.well.server.utils

import com.well.modules.models.UserId
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*

val ApplicationCall.authUid: UserId
    get() = principal<JWTPrincipal>()!!.payload.authUid!!

fun Payload.createPrincipal(dependencies: Dependencies) : JWTPrincipal? =
    authUid?.let {
        val userExists = dependencies.database
            .usersQueries
            .exists(it)
            .executeAsOne()
        if (userExists) JWTPrincipal(this) else null
    }

private val Payload.authUid: UserId?
    get() = claims[JwtConfig.uidKey]?.asInt()
