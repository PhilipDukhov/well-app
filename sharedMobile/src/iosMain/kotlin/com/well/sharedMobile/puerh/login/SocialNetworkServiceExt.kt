package com.well.sharedMobile.puerh.login

import com.well.sharedMobile.puerh._featureProvider.FeatureProvider
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("unused")
fun FeatureProvider.application(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>
) = socialNetworkService.credentialProviders.values.any { it.application(app, openURL, options) }