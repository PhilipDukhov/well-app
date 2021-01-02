package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.append
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

private enum class UserFields(val key: String) {
    FirstName("first_name"),
    LastName("last_name"),
    Picture("picture"),
}

suspend fun PipelineContext<*, ApplicationCall>.facebookLogin(
    dependencies: Dependencies
) = dependencies.run {
    getFacebookAuthenticatedClient().run {
        val facebookId = getUserId(call.receive())
        val id = getFacebookUserId(facebookId)
            ?: run {
                createFacebookUser(
                    facebookId,
                    getUserInfo(facebookId),
                )
            }.also {
                updateUserProfile(it, facebookId, dependencies)
            }
        call.respondAuthenticated(id, dependencies)
    }
}

private suspend fun HttpClient.getUserId(token: String): String =
    get<JsonElement>(
        "debug_token?input_token=$token"
    ).jsonObject
        .getValue("data")
        .jsonObject.getPrimitiveContent("user_id")

private suspend fun HttpClient.getUserInfo(userId: String): JsonObject =
    get<JsonElement>(userId) {
        parameter(
            "fields",
            UserFields
                .values()
                .map(UserFields::key)
                .joinToString(","),
        )
    }.jsonObject

private suspend fun HttpClient.getProfilePicture(userId: String) =
    get<JsonElement>("$userId/picture") {
        val pictureSize = 1000
        url.parameters.append(
            "height" to "$pictureSize",
            "width" to "$pictureSize",
            "redirect" to "0",
        )
    }.jsonObject["data"]
        ?.jsonObject
        ?.let {
            if (it["is_silhouette"]?.jsonPrimitive?.booleanOrNull != false) null
            else Url(it.getPrimitiveContent("url"))
        }

private fun Dependencies.getFacebookUserId(
    id: String,
) = database
    .userQueries
    .getByFacebookId(id)
    .executeAsOneOrNull()

private fun Dependencies.createFacebookUser(
    id: String,
    userInfo: JsonObject,
): UserId = database
    .userQueries
    .run {
        insertFacebook(
            userInfo.getPrimitiveContent(UserFields.FirstName),
            userInfo.getPrimitiveContent(UserFields.LastName),
            id,
        )
        lastInsertId()
            .executeAsOne()
            .toInt()
    }

private fun HttpClient.updateUserProfile(
    id: UserId,
    facebookId: String,
    dependencies: Dependencies,
) = CoroutineScope(Dispatchers.IO).launch {
    dependencies.run {
        (getProfilePicture(facebookId)
            ?.let { url -> uploadToS3FromUrl(url, "profilePictures/$id") }
            ?: getRandomPicture())
            .let {
                database
                    .userQueries
                    .updateProfileImage(it.toString(), id)
            }
    }
}

private fun JsonObject.getPrimitiveContent(field: UserFields) =
    getValue(field.key).jsonPrimitive.content