package com.well.auth.credentialProviders

import com.well.auth.Context
import com.well.auth.LoginView

expect class FacebookProvider(context: Context) : CredentialProvider {
    override suspend fun getCredentials(loginView: LoginView): AuthCredential
}