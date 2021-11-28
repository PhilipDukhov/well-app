package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.puerhBase.FeatureProvider
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("unused")
fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.application(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>,
) = (this as FeatureProviderImpl).run {
    socialNetworkService.credentialProviders.values
        .any { it.application(app, openURL, options) }
}