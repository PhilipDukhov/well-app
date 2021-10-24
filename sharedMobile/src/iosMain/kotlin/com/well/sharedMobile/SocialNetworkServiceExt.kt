package com.well.sharedMobile

import com.well.modules.utils.puerh.FeatureProvider
import com.well.sharedMobile.featureProvider.FeatureProviderImpl
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("unused")

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.handleActivityResult(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>
) = (this as FeatureProviderImpl).run {
    socialNetworkService.credentialProviders.values
        .any { it.application(app, openURL, options) }
}