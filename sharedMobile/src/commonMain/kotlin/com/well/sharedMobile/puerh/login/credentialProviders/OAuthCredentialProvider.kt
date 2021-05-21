package com.well.sharedMobile.puerh.login.credentialProviders

import com.well.sharedMobile.puerh._topLevel.ContextHelper

internal expect class OAuthCredentialProvider(
    name: String,
    contextHelper: ContextHelper,
) : CredentialProvider