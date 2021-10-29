package com.well.server.routing.auth

import com.well.modules.models.User
import com.well.server.utils.Dependencies
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun PipelineContext<*, ApplicationCall>.googleLogin(dependencies: Dependencies) =
    dependencies.run {
        val clientIds = environment
            .config
            .property("social.google.clientIds")
            .getList()
            .map { "$it.apps.googleusercontent.com" }
        val uid = withContext(Dispatchers.IO) {
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
            database.usersQueries.run {
                getByGoogleId(googleId)
                    .executeAsOneOrNull()
                    ?: run {
                        insertGoogle(
                            fullName = "${getValue("given_name") as String} ${getValue("family_name") as String}",
                            email = email,
                            type = User.Type.Doctor,
                            googleId = googleId
                        )
                        lastInsertId()
                            .executeAsOne()
                            .toInt()
                    }
            }
        }
        call.respondAuthenticated(uid, dependencies)
    }