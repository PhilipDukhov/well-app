package com.well.sharedMobile

import com.well.modules.puerhBase.FeatureProvider
import com.well.sharedMobile.featureProvider.FeatureProviderImpl
import android.content.Intent

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.handleActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?,
) = (this as FeatureProviderImpl).run {
    socialNetworkService
        .credentialProviders
        .values
        .any { it.handleActivityResult(requestCode, resultCode, data) }
}

fun FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>.handleOnNewIntent(
    data: Intent?,
) = (this as FeatureProviderImpl).run {
    socialNetworkService
        .credentialProviders
        .values
        .any { it.handleOnNewIntent(data) }
}