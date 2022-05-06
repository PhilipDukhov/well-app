package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.atomic.AtomicRef
import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.models.NetworkConstants
import com.well.modules.networking.createBaseServerClient
import com.well.modules.utils.viewUtils.AndroidRequestCodes
import com.well.modules.utils.viewUtils.SystemHelper
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isLocalServer
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
    private val systemHelper: SystemHelper,
) {
    private var getCredentialsJob by AtomicRef<Job>()
    private var getCredentialsContinuation by AtomicRef<CancellableContinuation<AuthCredential>>()

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
                                systemHelper
                                    .webAuthenticate(redirect.location, AndroidRequestCodes.OAuthHelper.code)
                            )
                        )
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
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
        } catch (_: Exception) { }
        return false
    }

    fun cancel() {
        getCredentialsContinuation?.cancel()
        getCredentialsJob?.cancel()
    }

    private fun processCallbackUrl(url: String): AuthCredential =
        NetworkConstants.current(Platform.isLocalServer).oauthCallbackPath().let { oauthCallbackPath ->
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