package com.well.modules.features.login.credentialProviders

import com.well.modules.viewHelpers.ContextHelper

expect class OAuthCredentialProvider(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider