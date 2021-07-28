package com.well.sharedMobile.puerh._topLevel

interface WebAuthenticator {
    suspend fun webAuthenticate(url: String, requestCode: Int): String
}