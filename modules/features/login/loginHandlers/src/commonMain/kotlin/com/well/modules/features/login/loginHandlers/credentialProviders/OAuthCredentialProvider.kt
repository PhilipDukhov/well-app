package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.utils.viewUtils.SystemHelper

expect class OAuthCredentialProvider(
    name: String,
    systemHelper: SystemHelper,
) : CredentialProvider