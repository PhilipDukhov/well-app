package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.AuthCredential
import com.well.modules.utils.viewUtils.AppContext
import android.content.Intent
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FacebookProvider constructor(private val appContext: AppContext) : CredentialProvider(appContext) {
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val loginManager = LoginManager.getInstance()

    fun finalize() {
        loginManager.unregisterCallback(callbackManager)
    }

    override suspend fun getCredentials(): AuthCredential =
        runCatching<AuthCredential> {
            suspendCancellableCoroutine { continuation ->
                loginManager.registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult) {
                            continuation.resume(AuthCredential.FacebookCredential(result.accessToken.token))
                        }

                        override fun onCancel() {
                            continuation.cancel()
                        }

                        override fun onError(error: FacebookException) {
                            continuation.resumeWithException(error)
                        }
                    }
                )
                loginManager.logInWithReadPermissions(appContext.androidContext, listOf("email"))
            }
        }.also {
            loginManager.unregisterCallback(callbackManager)
        }.getOrThrow()

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        callbackManager.onActivityResult(requestCode, resultCode, data)
}