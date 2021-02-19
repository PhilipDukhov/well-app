package com.well.sharedMobile.puerh.login.credentialProviders

import android.content.Intent
import com.well.utils.Context

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(): AuthCredential
    abstract fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}