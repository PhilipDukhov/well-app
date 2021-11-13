package com.well.server.routing.auth

import com.well.modules.models.User
import com.well.server.utils.Dependencies
import com.well.server.utils.append
import com.well.server.utils.getPrimitiveContent
import com.well.server.utils.uploadToS3FromUrl
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun PipelineContext<*, ApplicationCall>.facebookLogin(
    dependencies: Dependencies,
) = dependencies.run {
    getFacebookAuthenticatedClient().run {
        val facebookId = getUid(call.receive())
        val id = getFacebookUid(facebookId)
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

private suspend fun HttpClient.getUid(token: String): String =
    get<JsonElement>(
        "debug_token?input_token=$token"
    ).jsonObject
        .getValue("data")
        .jsonObject.getPrimitiveContent("user_id")

private suspend fun HttpClient.getUserInfo(uid: String): JsonObject =
    get<JsonElement>(uid) {
        parameter(
            "fields",
            UserFields
                .values()
                .map(UserFields::key)
                .joinToString(","),
        )
    }.jsonObject

private suspend fun HttpClient.getProfilePicture(uid: String) =
    get<JsonElement>("$uid/picture") {
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

private fun Dependencies.getFacebookUid(
    id: String,
) = database
    .usersQueries
    .getByFacebookId(id)
    .executeAsOneOrNull()

private fun Dependencies.createFacebookUser(
    id: String,
    userInfo: JsonObject,
): User.Id = database
    .usersQueries
    .run {
        val firstName = userInfo.getPrimitiveContent(UserFields.FirstName)
        val lastName = userInfo.getPrimitiveContent(UserFields.LastName)
        insertFacebook(
            fullName = "$firstName $lastName",
            type = User.Type.Doctor,
            email = userInfo.getNullablePrimitiveContent(UserFields.Email),
            facebookId = id,
        )
        User.Id(
            lastInsertId()
                .executeAsOne()
        )
    }

private suspend fun HttpClient.updateUserProfile(
    id: User.Id,
    facebookId: String,
    dependencies: Dependencies,
) = dependencies.apply {
    getProfilePicture(facebookId)
        ?.let { uploadToS3FromUrl(it, awsProfileImagePath(id, "")) }
        ?.let {
            database
                .usersQueries
                .updateProfileImage(it.toString(), id)
        }
}

private fun JsonObject.getPrimitiveContent(field: UserFields) =
    getNullablePrimitiveContent(field) ?: throw NoSuchElementException()

private fun JsonObject.getNullablePrimitiveContent(field: UserFields) =
    get(field.key)?.jsonPrimitive?.content

private enum class UserFields(val key: String) {
    Email("email"),
    FirstName("first_name"),
    LastName("last_name"),
    Picture("picture"),
}