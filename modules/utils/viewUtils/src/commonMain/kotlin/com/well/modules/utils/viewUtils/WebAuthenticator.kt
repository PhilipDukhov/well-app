package com.well.modules.utils.viewUtils

interface WebAuthenticator {
    suspend fun webAuthenticate(url: String, requestCode: Int): String
}