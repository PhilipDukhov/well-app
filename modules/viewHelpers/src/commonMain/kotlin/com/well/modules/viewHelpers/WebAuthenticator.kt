package com.well.modules.viewHelpers

interface WebAuthenticator {
    suspend fun webAuthenticate(url: String, requestCode: Int): String
}