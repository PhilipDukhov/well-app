package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.modules.atomic.AtomicRef
import com.well.sharedMobile.puerh._topLevel.ContextHelper
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual class OAuthCredentialProvider actual constructor(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider(contextHelper.context) {
    private val oAuthHelper = OAuthHelper(name, contextHelper)

    override suspend fun getCredentials() =
        oAuthHelper.getRequestTokenAndFollowRedirect()

    override fun application(
        app: UIApplication,
        openURL: NSURL,
        options: Map<Any?, *>
    ): Boolean {
        return oAuthHelper.handleCallbackUrl(openURL.absoluteString ?: return false)
    }
}