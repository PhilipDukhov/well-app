package com.well.server.utils

import com.well.modules.models.User
import com.auth0.jwt.interfaces.Payload
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

val ApplicationCall.authUid: User.Id
    get() = principal<JWTPrincipal>()!!.payload.authUid!!

fun Payload.createPrincipal(dependencies: Dependencies): JWTPrincipal? =
    authUid?.let {
        val userExists = dependencies.database
            .usersQueries
            .exists(it)
            .executeAsOne()
        if (userExists) JWTPrincipal(this) else null
    }

private val Payload.authUid: User.Id?
    get() = claims[JwtConfig.uidKey]?.asLong()?.let(User::Id)
