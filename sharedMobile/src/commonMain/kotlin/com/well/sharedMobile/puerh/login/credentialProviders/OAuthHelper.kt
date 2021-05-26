package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.modules.atomic.AtomicRef
import com.well.sharedMobile.networking.Constants
import com.well.sharedMobile.networking.createBaseServerClient
import com.well.sharedMobile.puerh._topLevel.ContextHelper
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class OAuthHelper(
    private val name: String,
    private val contextHelper: ContextHelper,
) {
    private var getCredentialsJob by AtomicRef<Job>()
    private var getCredentialsContinuation by AtomicRef<CancellableContinuation<AuthCredential>>()
    companion object {
        const val requestCode = 89781
    }

    private val client = createBaseServerClient().config {
        expectSuccess = false
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status != HttpStatusCode.Found) {
                    throw IllegalStateException("Redirect expected, but ${response.status} found")
                }
                val location = response.headers[HttpHeaders.Location]
                    ?: throw IllegalStateException("Redirect location expected")
                throw RedirectResponseException(location)
            }
        }
    }

    suspend fun getRequestTokenAndFollowRedirect(): AuthCredential =
        try {
            client.post<Unit>("login/$name")
            throw IllegalStateException("Redirect expected")
        } catch (redirect: RedirectResponseException) {
            suspendCancellableCoroutine { continuation ->
                getCredentialsContinuation = continuation
                getCredentialsJob = CoroutineScope(Dispatchers.Default).launch {
                    try {
                        continuation.resume(
                            processCallbackUrl(
                                contextHelper
                                    .webAuthenticate(redirect.location, requestCode)
                            )
                        )
                    } catch (t: Throwable) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(t)
                        }
                    }
                }
            }
        }

    fun handleCallbackUrl(url: String): Boolean {
        try {
            getCredentialsContinuation?.resume(
                processCallbackUrl(url)
            )
            getCredentialsJob?.cancel()
            return true
        } catch (t: Throwable) {

        }
        return false
    }

    fun cancel() {
        getCredentialsContinuation?.cancel()
        getCredentialsJob?.cancel()
    }

    private fun processCallbackUrl(url: String): AuthCredential =
        Constants.oauthCallbackPath().let { oauthCallbackPath ->
            when {
                url.startsWith(oauthCallbackPath) -> {
                    AuthCredential.OAuth1Credential(
                        url.substring(
                            oauthCallbackPath.length
                        )
                    )
                }
                else -> throw IllegalStateException("Callback url invalid")
            }
        }
    private data class RedirectResponseException(val location: String) : Exception()
}