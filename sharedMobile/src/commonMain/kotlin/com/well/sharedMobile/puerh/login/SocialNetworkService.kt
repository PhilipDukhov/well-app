package com.well.sharedMobile.puerh.login

import com.well.serverModels.User
import com.well.sharedMobile.networking.createBaseServerClient
import com.well.sharedMobile.puerh.login.credentialProviders.AuthCredential
import com.well.sharedMobile.puerh.login.credentialProviders.AuthCredential.*
import com.well.sharedMobile.puerh.login.credentialProviders.CredentialProvider
import com.well.utils.Context
import com.well.atomic.AtomicMutableMap
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class SocialNetworkService(private val providerGenerator: (SocialNetwork) -> CredentialProvider) {
    internal val credentialProviders = AtomicMutableMap<SocialNetwork, CredentialProvider>()
    private val client = createBaseServerClient()

    suspend fun login(
        network: SocialNetwork
    ) = getToken(
        credentialProviders.getOrPut(network) {
            providerGenerator(network)
        }.getCredentials()
    )

    private suspend fun getToken(
        credentials: AuthCredential
    ) = handleLoginResponse(
        when (credentials) {
            is GoogleCredential -> {
                client.post(path = "/googleLogin") {
                    body = credentials.token
                }
            }
            is FacebookCredential -> {
                client.post(path = "/facebookLogin") {
                    body = credentials.token
                }
            }
        }
    )

    suspend fun testLogin(uuid: String) =
        handleLoginResponse(
            client.post("testLogin") {
                body = uuid
            }
        )

    private fun handleLoginResponse(jsonElement: JsonElement): Pair<String, User> {
        val token = jsonElement.jsonObject["token"]!!
            .jsonPrimitive
            .content
        val user = Json.decodeFromString<User>(
            jsonElement.jsonObject["user"]!!.jsonPrimitive.content
        )
        return token to user
    }
}
