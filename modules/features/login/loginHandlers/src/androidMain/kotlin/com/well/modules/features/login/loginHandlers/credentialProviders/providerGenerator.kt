package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.WebAuthenticator

fun providerGenerator(
    socialNetwork: SocialNetwork,
    context: SystemContext,
    webAuthenticator: WebAuthenticator,
): CredentialProvider = when (socialNetwork) {
    SocialNetwork.Facebook -> FacebookProvider(context)
    SocialNetwork.Google -> GoogleProvider(context)
    SocialNetwork.Apple -> AppleOAuthProvider(
        systemContext = context,
        webAuthenticator = webAuthenticator,
    )
    SocialNetwork.Twitter,
    -> throw IllegalStateException("Twitter should be handler earlier")
}