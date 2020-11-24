package com.well.auth.credentialProviders

import com.well.auth.Context
import com.well.auth.LoginView
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(loginView: LoginView): AuthCredential

    abstract fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>): Boolean
}