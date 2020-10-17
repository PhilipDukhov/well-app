package com.well.androidApp.model.auth

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWebException
import com.google.firebase.auth.OAuthProvider
import com.well.androidApp.model.auth.credentialProviders.CredentialProvider
import com.well.androidApp.model.auth.credentialProviders.FacebookProvider
import com.well.androidApp.model.auth.credentialProviders.GoogleProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class SocialNetworkService(private val context: Context) {
    private val credentialProviders = mutableMapOf<SocialNetwork, CredentialProvider>()

    private val SocialNetwork.hasCredentialProvider: Boolean
        get() = when (this) {
            SocialNetwork.Google, SocialNetwork.Facebook -> true
            SocialNetwork.Twitter, SocialNetwork.Apple -> false
        }

    suspend fun login(
        network: SocialNetwork,
        fragment: Fragment
    ) = (
        if (network.hasCredentialProvider) {
            ::credentialsProviderLogin
        } else {
            ::oAuthProviderLogin
        }
        )(network, fragment)

    private suspend fun oAuthProviderLogin(
        network: SocialNetwork,
        fragment: Fragment
    ): AuthResult {
        val activity = fragment.activity ?: throw IllegalStateException()
        val builder = OAuthProvider.newBuilder(
            when (network) {
                SocialNetwork.Twitter -> "twitter.com"
                else -> throw IllegalArgumentException()
            }
        )
        try {
            return FirebaseAuth
                .getInstance()
                .startActivityForSignInWithProvider(activity, builder.build())
                .await()
        } catch (e: FirebaseAuthWebException) {
            throw when ((e).errorCode) {
                "ERROR_WEB_CONTEXT_CANCELED" -> CancellationException()
                else -> e
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun credentialsProviderLogin(
        network: SocialNetwork,
        fragment: Fragment
    ): AuthResult {
        val credentials = credentialProviders.getOrPut(network) {
            when (network) {
                SocialNetwork.Google -> GoogleProvider(context)
                SocialNetwork.Facebook -> FacebookProvider()
                else -> throw IllegalArgumentException()
            }
        }.getCredentials(fragment)
        return FirebaseAuth
            .getInstance()
            .signInWithCredential(credentials)
            .await()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        credentialProviders.values.first { it.handleActivityResult(requestCode, resultCode, data) }
    }
}
