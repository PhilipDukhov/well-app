package com.well.sharedMobile.puerh.login.credentialProviders

import android.content.Intent
import com.well.sharedMobile.puerh.login.Context
import com.well.sharedMobile.puerh.login.LoginView

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(loginView: LoginView): AuthCredential
    abstract fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}