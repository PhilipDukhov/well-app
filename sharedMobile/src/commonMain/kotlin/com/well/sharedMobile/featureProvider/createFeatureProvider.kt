package com.well.sharedMobile.featureProvider

import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.puerhBase.FeatureProvider
import com.well.modules.utils.viewUtils.WebAuthenticator
import com.well.sharedMobile.TopLevelFeature

fun createFeatureProvider(
    appContext: AppContext,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, AppContext, WebAuthenticator) -> CredentialProvider,
) : FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State> = FeatureProviderImpl(appContext, webRtcManagerGenerator, providerGenerator)