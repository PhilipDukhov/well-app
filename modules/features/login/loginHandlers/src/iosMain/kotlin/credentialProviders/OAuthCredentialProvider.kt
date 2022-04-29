package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.utils.viewUtils.SystemHelper
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class OAuthCredentialProvider actual constructor(
    name: String,
    systemHelper: SystemHelper,
) : CredentialProvider(systemHelper.systemContext) {
    private val oAuthHelper = OAuthHelper(name, systemHelper)

    override suspend fun getCredentials() =
        oAuthHelper.getRequestTokenAndFollowRedirect()

    override fun application(
        app: UIApplication,
        openURL: NSURL,
        options: Map<Any?, *>,
    ): Boolean {
        return oAuthHelper.handleCallbackUrl(openURL.absoluteString ?: return false)
    }
}