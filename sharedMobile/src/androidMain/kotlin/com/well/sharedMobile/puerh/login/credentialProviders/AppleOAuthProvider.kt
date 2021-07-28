package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.modules.utils.AppContext
import com.well.sharedMobile.networking.Constants
import com.well.sharedMobile.puerh._topLevel.WebAuthenticator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.well.sharedMobile.BuildKonfig

class AppleOAuthProvider(
    appContext: AppContext,
    private val webAuthenticator: WebAuthenticator,
) : CredentialProvider(appContext) {
    private val requestCode = 98234
    private var handleActivityResultCancelJob: Job? = null
    private var webAuthenticateJob: Job? = null
    private var getCredentialsContinuation: CancellableContinuation<AuthCredential>? = null

    override suspend fun getCredentials(): AuthCredential =
        suspendCancellableCoroutine {
            getCredentialsContinuation = it
            val url = Uri
                .parse("https://appleid.apple.com/auth/authorize")
                .buildUpon().apply {
                    appendQueryParameter("response_type", "code,id_token")
                    appendQueryParameter("client_id", BuildKonfig.apple_server_client_id)
                    appendQueryParameter("response_mode", "form_post")
                    appendQueryParameter("redirect_uri", BuildKonfig.apple_auth_redirect_url)
                    appendQueryParameter("scope", "email,name")
//                appendQueryParameter("state", state)
                }
                .build()
                .toString()
            webAuthenticateJob = CoroutineScope(Dispatchers.Default).launch {
                webAuthenticator.webAuthenticate(url, requestCode)
            }
        }


    override fun handleOnNewIntent(intent: Intent?): Boolean {
        handleActivityResultCancelJob?.cancel()
        handleActivityResultCancelJob = null
        val url = intent?.data?.toString() ?: return false
        Constants.oauthCallbackPath("apple").let { oauthCallbackPath ->
            return when {
                url.startsWith(oauthCallbackPath) -> {
                    TODO(
                        url.substring(
                            oauthCallbackPath.length
                        )
                    )
                    true
                }
                else -> false
            }
        }
    }

    override fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_CANCELED) {
            handleActivityResultCancelJob = CoroutineScope(Dispatchers.Default).launch {
                delay(100)
                getCredentialsContinuation?.cancel()
            }
            return true
        }
        return false
    }

    private fun processCallbackUrl(url: String): AuthCredential =
        Constants.oauthCallbackPath("apple").let { oauthCallbackPath ->
            when {
                url.startsWith(oauthCallbackPath) -> {
                    TODO()
                }
                else -> throw IllegalStateException("Callback url invalid")
            }
        }
}
