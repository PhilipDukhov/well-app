package com.well.server.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.config.*
import java.util.*

class JwtConfig(config: ApplicationConfig) {
    private val secret = config.property("jwt.accessTokenSecret").getString()

    val issuer = "ktor.io"
//    private val validityInMs = 10 * 60 * 60 * 1000 // 10 hours
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun makeToken(id: Int): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", id)
//        .withExpiresAt(getExpiration())
        .sign(algorithm)

//    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}