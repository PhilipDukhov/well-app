package com.well.modules.features.login.credentialProviders

import com.well.modules.utils.AppContext

expect abstract class CredentialProvider(appContext: AppContext) {
    abstract suspend fun getCredentials(): AuthCredential
}