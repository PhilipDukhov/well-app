@file:Suppress("unused")

package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.notifications.handleNotificationResponse
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.puerhBase.FeatureProvider
import io.github.aakira.napier.Napier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.application(
    app: UIApplication,
    openURL: NSURL,
    options: Map<Any?, *>,
) = (this as TopLevelFeatureProviderImpl).run {
    socialNetworkService
        ?.credentialProviders
        ?.values
        ?.any { it.application(app, openURL, options) }
        ?: false
}

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.userNotificationCenter(
    didReceiveNotificationResponse: platform.UserNotifications.UNNotificationResponse,
) {
    (this as TopLevelFeatureProviderImpl)
        .notificationHandler
        ?.handleNotificationResponse(didReceiveNotificationResponse)
        ?: run {
            Napier.d("notificationHandler not initialized")
        }
}