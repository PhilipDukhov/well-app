package com.well.auth

import com.well.auth.SocialNetwork.Facebook
import com.well.auth.SocialNetwork.Google
import com.well.auth.credentialProviders.AuthCredential
import com.well.auth.credentialProviders.AuthCredential.FacebookCredential
import com.well.auth.credentialProviders.AuthCredential.GoogleCredential
import com.well.auth.credentialProviders.CredentialProvider
import com.well.auth.credentialProviders.FacebookProvider
import com.well.auth.credentialProviders.GoogleProvider
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SocialNetworkService(private val context: Context) {
    internal val credentialProviders = mutableMapOf<SocialNetwork, CredentialProvider>()

    @Throws(Throwable::class)
    suspend fun login(
        network: SocialNetwork,
        loginView: LoginView
    ): AuthResult {
        val credentials = credentialProviders.getOrPut(network) {
            when (network) {
                Google -> GoogleProvider(context)
                Facebook -> FacebookProvider(context)
            }
        }.getCredentials(loginView)
        return getToken(credentials)
    }

    private suspend fun getToken(
        credentials: AuthCredential
    ): AuthResult {
        val client = HttpClient {
            defaultRequest {
                host = "dukhovwellserver.com"
                port = 8090
            }
        }
        val tokenResponse: String = when (credentials) {
            is GoogleCredential -> {
                client.post {
                    url.encodedPath = "/googleLogin"
                    body = credentials.token
                }
            }
            is FacebookCredential -> {
                client.post {
                    url.encodedPath = "/facebookLogin"
                    body = credentials.token
                }
            }
        }
        val token = Json.parseToJsonElement(tokenResponse)
            .jsonObject["token"]!!
            .jsonPrimitive.content
        return AuthResult(token)
    }
}
