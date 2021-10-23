package com.well.modules.features.login

import com.well.modules.networking.createBaseServerClient
import com.well.modules.features.login.credentialProviders.AuthCredential
import com.well.modules.features.login.credentialProviders.AuthCredential.*
import com.well.modules.features.login.credentialProviders.CredentialProvider
import com.well.modules.atomic.AtomicMutableMap
import com.well.modules.models.AuthResponse
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke

internal class SocialNetworkService(private val providerGenerator: (SocialNetwork) -> CredentialProvider) {
    internal val credentialProviders = AtomicMutableMap<SocialNetwork, CredentialProvider>()
    private val client = createBaseServerClient()

    suspend fun login(
        network: SocialNetwork
    ) = Dispatchers.Main.invoke {
        getToken(
            credentialProviders.getOrPut(network) {
                providerGenerator(network)
            }.getCredentials()
        )
    }

    private suspend fun getToken(
        credentials: AuthCredential
    ): AuthResponse = when (credentials) {
        is GoogleCredential -> {
            client.post(path = "login/google") {
                body = credentials.token
            }
        }
        is FacebookCredential -> {
            client.post(path = "login/facebook") {
                body = credentials.token
            }
        }
        is AppleCredential -> {
            client.post(path = "login/apple") {
                header(HttpHeaders.Authorization, "Bearer ${credentials.identityToken}")
            }
        }
        is OAuth1Credential -> {
            client.post("login/twitter${credentials.params}")
        }
    }
}