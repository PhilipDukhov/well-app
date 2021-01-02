package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.getPrimitiveContent
import com.well.server.utils.uploadToS3FromUrl
import com.well.serverModels.UserId
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

suspend fun PipelineContext<*, ApplicationCall>.testLogin(
    dependencies: Dependencies,
) = dependencies.run {
    val deviceId = call.receive<String>()
    val id = getTestUserId(deviceId)
        ?: createTestUser(
            deviceId,
            client.getRandomUser()
        ).also { id ->
            CoroutineScope(Dispatchers.IO).launch {
                dependencies.run {
                    getRandomPicture()
                        .let {
                            database
                                .userQueries
                                .updateProfileImage(it.toString(), id)
                        }
                }
            }
        }

    call.respondAuthenticated(id, dependencies)
}

private suspend fun HttpClient.getRandomUser() =
    get<JsonElement>(
        "http://names.drycodes.com/1?nameOptions=boy_names"
    ).jsonArray[0]
        .jsonPrimitive
        .content
        .split("_")
        .zipWithNext()[0]

private fun Dependencies.getTestUserId(
    id: String,
) = database
    .userQueries
    .getByTestId(id)
    .executeAsOneOrNull()

private fun Dependencies.createTestUser(
    id: String,
    fullName: Pair<String, String>,
): UserId = database
    .userQueries
    .run {
        insertTestId(
            fullName.first,
            fullName.second,
            id,
        )
        lastInsertId()
            .executeAsOne()
            .toInt()
    }