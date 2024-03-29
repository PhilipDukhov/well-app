package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.notifications.handleOnNewIntent
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.puerhBase.FeatureProvider
import android.content.Intent

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.handleActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
) = (this as TopLevelFeatureProviderImpl).run {
    socialNetworkService
        ?.credentialProviders
        ?.values
        ?.any { it.handleActivityResult(requestCode, resultCode, data) }
        ?: false
}

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.handleOnNewIntent(
    data: Intent,
) = (this as TopLevelFeatureProviderImpl).run {
    notificationHandler
        ?.handleOnNewIntent(data)
        ?: socialNetworkService
            ?.credentialProviders
            ?.values
            ?.any { it.handleOnNewIntent(data) }
        ?: false
}