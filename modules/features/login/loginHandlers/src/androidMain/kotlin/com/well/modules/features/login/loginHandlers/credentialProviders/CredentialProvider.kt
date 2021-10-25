package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.AuthCredential
import com.well.modules.utils.viewUtils.AppContext
import android.content.Intent

actual abstract class CredentialProvider actual constructor(appContext: AppContext) {
    actual abstract suspend fun getCredentials(): AuthCredential
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = false
    open fun handleOnNewIntent(intent: Intent?) = false
}