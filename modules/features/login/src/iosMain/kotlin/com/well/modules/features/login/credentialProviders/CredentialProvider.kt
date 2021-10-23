package com.well.modules.features.login.credentialProviders

import com.well.modules.utils.AppContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class CredentialProvider actual constructor(appContext: AppContext) {
    actual abstract suspend fun getCredentials(): AuthCredential

    open fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>) = false
}