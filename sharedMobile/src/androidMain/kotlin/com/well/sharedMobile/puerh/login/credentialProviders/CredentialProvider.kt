package com.well.sharedMobile.puerh.login.credentialProviders

import android.content.Intent
import com.well.modules.utils.Context

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(): AuthCredential
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = false
    open fun handleOnNewIntent(intent: Intent?) = false
}