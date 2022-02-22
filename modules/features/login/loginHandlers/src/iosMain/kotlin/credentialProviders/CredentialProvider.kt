package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.utils.viewUtils.SystemContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class CredentialProvider actual constructor(systemContext: SystemContext) {
    actual abstract suspend fun getCredentials(): AuthCredential

    open fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>) = false
}