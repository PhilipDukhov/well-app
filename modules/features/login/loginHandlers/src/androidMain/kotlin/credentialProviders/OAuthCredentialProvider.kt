package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.utils.viewUtils.AndroidRequestCodes
import com.well.modules.utils.viewUtils.SystemHelper
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class OAuthCredentialProvider actual constructor(
    name: String,
    systemHelper: SystemHelper,
) : CredentialProvider(systemHelper.systemContext) {
    private val oAuthHelper = OAuthHelper(name, systemHelper)
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
        if (requestCode == com.well.modules.utils.viewUtils.AndroidRequestCodes.OAuthHelper.code && resultCode == RESULT_CANCELED) {
            handleActivityResultCancelJob = CoroutineScope(Dispatchers.Default).launch {
                delay(100)
                oAuthHelper.cancel()
            }
            return true
        }
        return false
    }
}