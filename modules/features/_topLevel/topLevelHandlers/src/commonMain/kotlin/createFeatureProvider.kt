package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.puerhBase.FeatureProvider
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.WebAuthenticator

fun createFeatureProvider(
    applicationContext: ApplicationContext,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, SystemContext, WebAuthenticator) -> CredentialProvider,
) : FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State> = TopLevelFeatureProviderImpl(applicationContext, webRtcManagerGenerator, providerGenerator)