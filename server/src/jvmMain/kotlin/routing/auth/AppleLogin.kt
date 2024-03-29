package com.well.server.routing.auth

import com.well.modules.models.User
import com.well.server.utils.Services
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.appleLoginPrincipal(
    services: Services,
) = services.run {
    val claims = call.principal<JWTPrincipal>()!!.payload.claims
    val appleId = claims["sub"]?.asString() ?: run {
        call.respond(HttpStatusCode.Unauthorized, "payload missing 'sub'")
        return@appleLoginPrincipal
    }
    val id = getByAppleId(appleId)
        ?: run {
            createWithAppleId(
                appleId,
                claims["email"]?.asString(),
            )
        }
    call.respondAuthenticated(id, services)
}

private suspend fun PipelineContext<*, ApplicationCall>.appleLoginParams(
    services: Services,
) = services.run {
    val params = call.receive<String>()
        .parseUrlEncodedParameters()
    val error = params["error"]
    if (error != null) {
        call.respond(HttpStatusCode.BadRequest, error)
        return@appleLoginParams
    }
    val idToken = params["id_token"]!!

}

private fun Services.getByAppleId(id: String) =
    database.usersQueries.getByAppleId(id).executeAsOneOrNull()

private fun Services.createWithAppleId(id: String, email: String?): User.Id =
    database.usersQueries.run {
        insertApple(
            appleId = id,
            fullName = "",
            type = User.Type.Doctor,
            email = email,
        )
        User.Id(
            lastInsertId()
                .executeAsOne()
        )
    }

data class AppleUserSignInRequest(
    val code: String,
    val nick: String,
)

data class Oauth2Parameters(
    val grantType: String,
    val code: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String?,
)

@OptIn(InternalAPI::class)
class Oauth2Service(val client: HttpClient) {
    suspend inline fun authenticate(
        tokenUrl: String,
        parameters: Oauth2Parameters,
    ): AppleOauthResponse {
        val data = Parameters.build {
            append("grant_type", parameters.grantType)
            append("code", parameters.code)
            parameters.redirectUri?.apply { append("redirect_uri", parameters.redirectUri) }
            append("client_id", parameters.clientId)
            append(
                "client_secret", parameters.clientSecret
            )
        }

        println("Sending form with data: $data")
        val body = client.submitForm<AppleOauthResponse>(
            url = tokenUrl,
            formParameters = data,
            encodeInQuery = false,
//            block = { header("user-agent", "cheer-with-me") }
        )

        println(body)
        return body
    }
}

data class AppleOauthResponse(
    val access_token: String,
    val expires_in: Long,
    val id_token: String,
    val refresh_token: String,
    val token_type: String,
)

@OptIn(InternalAPI::class)
fun Parameters.Companion.build(list: List<Pair<String, String>>) =
    build {
        list.forEach {
            append(it.first, it.second)
        }
    }
