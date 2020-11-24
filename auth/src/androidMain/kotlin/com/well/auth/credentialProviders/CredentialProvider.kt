package com.well.auth.credentialProviders

import android.content.Intent
import com.well.auth.Context
import com.well.auth.LoginView

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(loginView: LoginView): AuthCredential
    abstract fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}