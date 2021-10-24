package com.well.sharedMobile.featureProvider

import com.well.modules.features.call.webRtc.WebRtcManagerI
import com.well.modules.features.login.SocialNetwork
import com.well.modules.features.login.credentialProviders.CredentialProvider
import com.well.modules.utils.AppContext
import com.well.modules.utils.puerh.FeatureProvider
import com.well.modules.viewHelpers.WebAuthenticator
import com.well.sharedMobile.TopLevelFeature

fun createFeatureProvider(
    appContext: AppContext,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, AppContext, WebAuthenticator) -> CredentialProvider,
) : FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State> = FeatureProviderImpl(appContext, webRtcManagerGenerator, providerGenerator)