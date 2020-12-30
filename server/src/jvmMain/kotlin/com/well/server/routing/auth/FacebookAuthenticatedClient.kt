package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.append
import com.well.server.utils.configProperty
import com.well.server.utils.getPrimitiveContent
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

private lateinit var facebookAuthenticatedClient: HttpClient

suspend fun Dependencies.getFacebookAuthenticatedClient(): HttpClient {
    if (::facebookAuthenticatedClient.isInitialized) return facebookAuthenticatedClient
    val appId = environment.configProperty("facebook.appId")
    val appSecret = environment.configProperty("facebook.appSecret")
    val host = "graph.facebook.com"
    val client = localClient.config {
        defaultRequest {
            if (url.host == "localhost") {
                url.host = host
                url.protocol = URLProtocol.HTTPS
                url.encodedPath = "/v9.0" + url.encodedPath
            }
        }
    }
    val accessTokenKey = "access_token"
    val appAccessToken = client.get<JsonElement>(
        "oauth/access_token"
    ) {
        url.parameters.append(
            "client_id" to appId,
            "client_secret" to appSecret,
            "grant_type" to "client_credentials"
        )
    }.jsonObject
        .getPrimitiveContent(accessTokenKey)
    facebookAuthenticatedClient = client.config {
        defaultRequest {
            if (url.host == host) {
                parameter(accessTokenKey, appAccessToken)
            }
        }
    }
    return facebookAuthenticatedClient
}