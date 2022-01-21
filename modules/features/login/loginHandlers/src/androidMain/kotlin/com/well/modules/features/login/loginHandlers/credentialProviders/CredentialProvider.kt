package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.utils.viewUtils.SystemContext
import android.content.Intent

actual abstract class CredentialProvider actual constructor(systemContext: SystemContext) {
    actual abstract suspend fun getCredentials(): AuthCredential
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = false
    open fun handleOnNewIntent(intent: Intent?) = false
}