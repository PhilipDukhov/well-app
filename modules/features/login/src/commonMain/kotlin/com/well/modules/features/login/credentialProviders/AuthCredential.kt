package com.well.modules.features.login.credentialProviders

sealed class AuthCredential {
    class FacebookCredential(val token: String) : AuthCredential()
    class AppleCredential(val identityToken: String) : AuthCredential()
    class GoogleCredential(val token: String) : AuthCredential()
    class OAuth1Credential(val params: String) : AuthCredential()
}