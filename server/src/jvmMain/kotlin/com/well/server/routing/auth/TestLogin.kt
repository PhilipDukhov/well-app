package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.serverModels.User
import com.well.serverModels.UserId
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun PipelineContext<*, ApplicationCall>.testLogin(
    dependencies: Dependencies,
) = dependencies.run {
    val deviceId = call.receive<String>()
    val id = getTestUserId(deviceId)
        ?: createTestUser(
            deviceId,
            client.getRandomUser()
        ).also { id ->
            dependencies.run {
                getRandomPicture()
                    .let {
                        database
                            .userQueries
                            .updateProfileImage(it.toString(), id)
                    }
            }
        }
    call.respondAuthenticated(id, dependencies)
}

private suspend fun HttpClient.getRandomUser() =
    get<String>(
        "https://api.namefake.com/english-united-states/male/"
    ).let {
        val jsonElement = Json.decodeFromString<JsonElement>(it)
        jsonElement.jsonObject["name"]!!
            .jsonPrimitive
            .content
    }

private fun Dependencies.getTestUserId(
    id: String,
) = database
    .userQueries
    .getByTestId(id)
    .executeAsOneOrNull()

private fun Dependencies.createTestUser(
    id: String,
    fullName: String,
): UserId = database
    .userQueries
    .run {
        insertTestId(
            fullName = fullName,
            type = User.Type.Doctor,
            testId = id,
        )
        lastInsertId()
            .executeAsOne()
            .toInt()
    }