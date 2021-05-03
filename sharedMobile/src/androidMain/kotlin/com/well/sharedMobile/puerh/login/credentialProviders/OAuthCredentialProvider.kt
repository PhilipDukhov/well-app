package com.well.sharedMobile.puerh.login.credentialProviders

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import com.well.sharedMobile.puerh._topLevel.ContextHelper
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal actual class OAuthCredentialProvider actual constructor(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider(contextHelper.context) {
    private val oAuthHelper = OAuthHelper(name, contextHelper)
    private var handleActivityResultCancelJob: Job? = null

    override suspend fun getCredentials() =
        oAuthHelper.getRequestTokenAndFollowRedirect()

    override fun handleOnNewIntent(intent: Intent?): Boolean {
        handleActivityResultCancelJob?.cancel()
        handleActivityResultCancelJob = null
        return oAuthHelper.handleCallbackUrl(intent?.data?.toString() ?: return false)
    }

    override fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        if (requestCode == OAuthHelper.requestCode && resultCode == RESULT_CANCELED) {
            handleActivityResultCancelJob = CoroutineScope(Dispatchers.Default).launch {
                delay(100)
                oAuthHelper.cancel()
            }
            return true
        }
        return false
    }
}