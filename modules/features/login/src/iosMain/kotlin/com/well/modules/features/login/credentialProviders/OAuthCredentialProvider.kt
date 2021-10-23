package com.well.modules.features.login.credentialProviders

import com.well.sharedMobile.ContextHelper
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual class OAuthCredentialProvider actual constructor(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider(contextHelper.appContext) {
    private val oAuthHelper = OAuthHelper(name, contextHelper)

    override suspend fun getCredentials() =
        oAuthHelper.getRequestTokenAndFollowRedirect()

    override fun application(
        app: UIApplication,
        openURL: NSURL,
        options: Map<Any?, *>
    ): Boolean {
        return oAuthHelper.handleCallbackUrl(openURL.absoluteString ?: return false)
    }
}