package com.well.auth.credentialProviders

import com.well.auth.Context
import com.well.auth.LoginView

expect abstract class CredentialProvider(context: Context) {
    abstract suspend fun getCredentials(loginView: LoginView): AuthCredential
}
