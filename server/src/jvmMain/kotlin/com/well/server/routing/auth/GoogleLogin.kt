package com.well.server.routing.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun PipelineContext<*, ApplicationCall>.googleLogin(dependencies: Dependencies) {
    val clientIds = dependencies
        .environment
        .config
        .property("google.clientIds")
        .getList()
        .map { "$it.apps.googleusercontent.com" }
    val userId = withContext(Dispatchers.IO) {
        @Suppress("BlockingMethodInNonBlockingContext")
        GoogleIdTokenVerifier.Builder(
            ApacheHttpTransport(),
            JacksonFactory()
        ).setAudience(clientIds)
            .build()
            .verify(call.receive<String>())
    }.payload.run {
        val googleId = subject
        dependencies.database.userQueries.run {
            getByGoogleId(googleId)
                .executeAsOneOrNull()
                ?: run {
                    insertGoogle(
                        getValue("given_name") as String,
                        getValue("family_name") as String,
                        googleId
                    )
                    lastInsertId()
                        .executeAsOne()
                        .toInt()
                }
        }
    }
    call.respond(HttpStatusCode.Created, mapOf("token" to dependencies.jwtConfig.makeToken(userId)))
}