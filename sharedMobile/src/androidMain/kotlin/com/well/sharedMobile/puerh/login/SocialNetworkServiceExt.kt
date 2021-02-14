package com.well.sharedMobile.puerh.login

import android.content.Intent

fun SocialNetworkService.handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    credentialProviders.values.first { it.handleActivityResult(requestCode, resultCode, data) }
}