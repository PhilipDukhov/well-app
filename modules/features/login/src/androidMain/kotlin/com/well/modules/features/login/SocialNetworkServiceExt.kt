package com.well.modules.features.login

import android.content.Intent
import com.well.sharedMobile.featureProvider.FeatureProvider

fun FeatureProvider.handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
    socialNetworkService
        .credentialProviders
        .values
        .any { it.handleActivityResult(requestCode, resultCode, data) }

fun FeatureProvider.handleOnNewIntent(data: Intent?) =
    socialNetworkService
        .credentialProviders
        .values
        .any { it.handleOnNewIntent(data) }