package com.well.modules.features.login

import com.well.sharedMobile.featureProvider.FeatureProvider
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("unused")
fun FeatureProvider.application(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>
) = socialNetworkService.credentialProviders.values.any { it.application(app, openURL, options) }