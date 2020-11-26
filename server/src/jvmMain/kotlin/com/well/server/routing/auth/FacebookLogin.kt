package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.getPrimitiveContent
import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

suspend fun PipelineContext<*, ApplicationCall>.facebookLogin(dependencies: Dependencies) {
    val token = call.receive<String>()
    val appId = dependencies.environment.config.property("facebook.appId").getString()
    val appSecret = dependencies.environment.config.property("facebook.appSecret").getString()
    val appAccessToken = dependencies.client.get<JsonElement>(
        "https://graph.facebook.com/oauth/access_token?client_id=$appId&client_secret=$appSecret&grant_type=client_credentials"
    ).jsonObject
        .getPrimitiveContent("access_token")
    val facebookUserId = dependencies.client.get<JsonElement>(
        "https://graph.facebook.com/debug_token?input_token=$token&access_token=$appAccessToken"
    ).jsonObject
        .getValue("data")
        .jsonObject.getPrimitiveContent("user_id")

    val fields = object {
        val id = "id"
        val firstName = "first_name"
        val lastName = "last_name"
    }
    val userInfo: JsonElement = dependencies.client.get(
        "https://graph.facebook.com/v9.0/$facebookUserId?fields=${fields.firstName},${fields.lastName},${fields.id}&access_token=$appAccessToken"
    )

    val userId = userInfo.jsonObject.let { json ->
        val facebookId = json.getPrimitiveContent(fields.id)
        dependencies.database.userQueries.run {
            getByFacebookId(facebookId)
                .executeAsOneOrNull()
                ?: run {
                    insertFacebook(
                        json.getPrimitiveContent(fields.firstName),
                        json.getPrimitiveContent(fields.lastName),
                        facebookId
                    )
                    lastInsertId()
                        .executeAsOne()
                        .toInt()
                }
        }
    }

    call.respond(HttpStatusCode.Created, mapOf("token" to dependencies.jwtConfig.makeToken(userId)))
}