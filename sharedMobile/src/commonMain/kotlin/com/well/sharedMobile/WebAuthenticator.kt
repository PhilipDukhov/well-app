package com.well.sharedMobile

interface WebAuthenticator {
    suspend fun webAuthenticate(url: String, requestCode: Int): String
}