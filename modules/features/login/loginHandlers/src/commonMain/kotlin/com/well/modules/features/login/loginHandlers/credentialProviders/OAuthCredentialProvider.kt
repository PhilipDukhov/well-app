package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.utils.viewUtils.ContextHelper

expect class OAuthCredentialProvider(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider