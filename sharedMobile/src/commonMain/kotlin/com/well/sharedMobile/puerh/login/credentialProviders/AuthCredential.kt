package com.well.sharedMobile.puerh.login.credentialProviders

sealed class AuthCredential {
    class FacebookCredential(val token: String) : AuthCredential()
    class AppleCredential(val userIdentifier: String) : AuthCredential()
    class GoogleCredential(val token: String) : AuthCredential()
    class OAuth1Credential(val params: String) : AuthCredential()
}