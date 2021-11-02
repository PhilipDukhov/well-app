package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.utils.viewUtils.AppContext

expect abstract class CredentialProvider(appContext: AppContext) {
    abstract suspend fun getCredentials(): AuthCredential
}