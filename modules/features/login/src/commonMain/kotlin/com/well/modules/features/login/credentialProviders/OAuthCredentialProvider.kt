package com.well.modules.features.login.credentialProviders

import com.well.sharedMobile.ContextHelper

internal expect class OAuthCredentialProvider(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider