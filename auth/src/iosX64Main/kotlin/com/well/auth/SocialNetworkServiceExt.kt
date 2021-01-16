package com.well.auth

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

fun SocialNetworkService.application(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>
): Boolean =
    credentialProviders.values.any { it.application(app, openURL, options) }