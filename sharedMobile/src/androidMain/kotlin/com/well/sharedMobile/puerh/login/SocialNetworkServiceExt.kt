package com.well.sharedMobile.puerh.login

import android.content.Intent
import com.well.sharedMobile.puerh._featureProvider.FeatureProvider

fun FeatureProvider.handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
    socialNetworkService
        .credentialProviders
        .values
        .any { it.handleActivityResult(requestCode, resultCode, data) }
