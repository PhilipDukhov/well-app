package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.utils.viewUtils.SystemContext

expect abstract class CredentialProvider(systemContext: SystemContext) {
    abstract suspend fun getCredentials(): AuthCredential
}