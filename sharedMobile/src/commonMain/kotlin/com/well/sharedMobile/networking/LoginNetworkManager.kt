package com.well.sharedMobile.networking

import io.ktor.client.request.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LoginNetworkManager {
    suspend fun testLogin(uuid: String) =
        parseToken(
            createBaseServerClient().post("testLogin") {
                body = uuid
            }
        )

    private fun parseToken(jsonElement: JsonElement): String =
        jsonElement.jsonObject["token"]!!
            .jsonPrimitive
            .content
}