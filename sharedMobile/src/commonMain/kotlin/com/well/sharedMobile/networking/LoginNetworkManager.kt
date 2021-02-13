package com.well.sharedMobile.networking

import com.well.serverModels.User
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class LoginNetworkManager {
    suspend fun testLogin(uuid: String) =
        createBaseServerClient().post<JsonElement>("testLogin") {
            body = uuid
        }.let { jsonElement ->
            val token = jsonElement.jsonObject["token"]!!
                .jsonPrimitive
                .content
            val user = Json.decodeFromString<User>(
                jsonElement.jsonObject["user"]!!.jsonPrimitive.content
            )
            token to user
        }
}