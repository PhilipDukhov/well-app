package com.well.sharedMobile.puerh.login.credentialProviders

import android.content.Intent
import com.well.modules.utils.AppContext

actual abstract class CredentialProvider actual constructor(appContext: AppContext) {
    actual abstract suspend fun getCredentials(): AuthCredential
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = false
    open fun handleOnNewIntent(intent: Intent?) = false
}