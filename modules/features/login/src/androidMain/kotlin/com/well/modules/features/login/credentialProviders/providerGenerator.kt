package com.well.modules.features.login.credentialProviders

import com.well.modules.features.login.SocialNetwork
import com.well.modules.utils.AppContext
import com.well.modules.viewHelpers.WebAuthenticator

fun providerGenerator(
    socialNetwork: SocialNetwork,
    context: AppContext,
    webAuthenticator: WebAuthenticator
): CredentialProvider = when (socialNetwork) {
    SocialNetwork.Facebook -> FacebookProvider(context)
    SocialNetwork.Google -> GoogleProvider(context)
    SocialNetwork.Apple -> AppleOAuthProvider(
        appContext = context,
        webAuthenticator = webAuthenticator,
    )
    SocialNetwork.Twitter
    -> throw IllegalStateException("Twitter should be handler earlier")
}