package com.well.auth

import android.content.Intent

fun SocialNetworkService.handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    credentialProviders.values.first { it.handleActivityResult(requestCode, resultCode, data) }
}