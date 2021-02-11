package com.well.server.routing.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun PipelineContext<*, ApplicationCall>.googleLogin(dependencies: Dependencies) =
    dependencies.run {
        val clientIds = environment
            .config
            .property("google.clientIds")
            .getList()
            .map { "$it.apps.googleusercontent.com" }
        val userId = withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            GoogleIdTokenVerifier.Builder(
                ApacheHttpTransport(),
                GsonFactory()
            )
                .setAudience(clientIds)
                .build()
                .verify(call.receive<String>())
        }.payload.run {
            val googleId = subject
            database.userQueries.run {
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
                    }.also {
                        database
                            .userQueries
                            .updateProfileImage(
                                getRandomPicture()
                                    .toString(),
                                it
                            )
                    }
            }
        }
        call.respondAuthenticated(userId, dependencies)
    }