package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.modules.utils.Context
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class CredentialProvider actual constructor(context: Context) {
    actual abstract suspend fun getCredentials(): AuthCredential

    abstract fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>): Boolean
}