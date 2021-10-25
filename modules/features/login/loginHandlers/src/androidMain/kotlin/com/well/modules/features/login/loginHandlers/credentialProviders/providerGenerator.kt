package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.WebAuthenticator

fun providerGenerator(
    socialNetwork: SocialNetwork,
    context: AppContext,
    webAuthenticator: WebAuthenticator
): CredentialProvider = when (socialNetwork) {
    com.well.modules.features.login.loginFeature.SocialNetwork.Facebook -> FacebookProvider(context)
    com.well.modules.features.login.loginFeature.SocialNetwork.Google -> GoogleProvider(context)
    com.well.modules.features.login.loginFeature.SocialNetwork.Apple -> AppleOAuthProvider(
        appContext = context,
        webAuthenticator = webAuthenticator,
    )
    com.well.modules.features.login.loginFeature.SocialNetwork.Twitter
    -> throw IllegalStateException("Twitter should be handler earlier")
}