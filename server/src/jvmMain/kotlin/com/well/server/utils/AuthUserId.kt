package com.well.server.utils

import com.auth0.jwt.interfaces.Payload
import com.well.serverModels.UserId
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*

val ApplicationCall.authUserId: UserId
    get() = principal<JWTPrincipal>()!!.payload.claims["id"]!!.asInt()

fun Payload.createPrincipal(dependencies: Dependencies) : JWTPrincipal? =
    claims["id"]?.asInt()?.let {
        println("$it")
        val userExists = dependencies.database
            .userQueries
            .exists(it)
            .executeAsOne()
        println("found $userExists")
        if (userExists) JWTPrincipal(this) else null
    }
