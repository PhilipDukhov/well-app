package com.well.server.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.config.*
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.StringReader
import java.security.interfaces.ECPrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class JwtConfig(secret: String) {
    val issuer = "ktor.io"

    companion object {
        const val uidKey = "id"
    }

    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun makeToken(id: Int): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim(uidKey, id)
        .sign(algorithm)

    fun createAppleAuthToken(
        keyId: String,
        privateKeyString: String,
        teamId: String,
    ): String {
        val pemParser = PEMParser(StringReader(privateKeyString))
        val privateKey = JcaPEMKeyConverter().getPrivateKey(pemParser.readObject() as PrivateKeyInfo)
        val algorithm = Algorithm.ECDSA256(null, privateKey as ECPrivateKey)
        return JWT.create()
            .withIssuer(teamId)
            .withKeyId(keyId)
            .withExpiresAt(Date.from(Instant.now().plus(180, ChronoUnit.DAYS)))
            .sign(algorithm)
    }
}