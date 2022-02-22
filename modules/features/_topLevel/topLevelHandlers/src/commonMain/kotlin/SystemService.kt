package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginHandlers.SocialNetworkService
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.features.login.loginHandlers.credentialProviders.OAuthCredentialProvider
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.WebAuthenticator
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler

class SystemService(
    val context: SystemContext,
    providerGenerator: (SocialNetwork, SystemContext, WebAuthenticator) -> CredentialProvider,
) {
    val permissionsHandler = PermissionsHandler(context.permissionHandlerContext)
    val socialNetworkService = SocialNetworkService { network ->
        when (network) {
            SocialNetwork.Twitter -> {
                OAuthCredentialProvider("twitter", context.helper)
            }
            else -> providerGenerator(network, context, context.helper)
        }
    }
}