package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.modules.utils.Context

expect abstract class CredentialProvider(context: Context) {
    abstract suspend fun getCredentials(): AuthCredential
}
