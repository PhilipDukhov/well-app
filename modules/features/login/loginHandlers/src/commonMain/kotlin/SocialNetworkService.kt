package com.well.modules.features.login.loginHandlers

import com.well.modules.atomic.AtomicMutableMap
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential.AppleCredential
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential.FacebookCredential
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential.GoogleCredential
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential.OAuth1Credential
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.models.AuthResponse
import com.well.modules.networking.createBaseServerClient
import io.github.aakira.napier.Napier
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke

class SocialNetworkService(private val providerGenerator: (SocialNetwork) -> CredentialProvider) {
    val credentialProviders = AtomicMutableMap<SocialNetwork, CredentialProvider>()
    private val client = createBaseServerClient()

    suspend fun login(
        network: SocialNetwork,
    ) = Dispatchers.Main.invoke {
        getToken(
            credentialProviders.getOrPut(network) {
                providerGenerator(network)
            }.getCredentials()
        )
    }

    private suspend fun getToken(
        credentials: AuthCredential,
    ): AuthResponse = when (credentials) {
        is GoogleCredential -> {
            client.post("login/google") {
                setBody(credentials.token)
            }
        }
        is FacebookCredential -> {
            client.post("login/facebook") {
                setBody(credentials.token)
            }
        }
        is AppleCredential -> {
            client.post("login/apple") {
                header(HttpHeaders.Authorization, "Bearer ${credentials.identityToken}")
            }
        }
        is OAuth1Credential -> {
            client.post("login/twitter${credentials.params}")
        }
    }.body()
}