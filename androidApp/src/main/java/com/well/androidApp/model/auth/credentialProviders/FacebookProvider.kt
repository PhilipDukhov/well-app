package com.well.androidApp.model.auth.credentialProviders

import android.content.Intent
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FacebookProvider : CredentialProvider {
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val loginManager = LoginManager.getInstance()

    fun finalize() {
        loginManager.unregisterCallback(callbackManager)
    }

    override suspend fun getCredentials(fragment: Fragment): AuthCredential =
        runCatching<AuthCredential> {
            suspendCancellableCoroutine { continuation ->
                loginManager.registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult?) {
                            if (result != null) {
                                continuation.resume(FacebookAuthProvider.getCredential(result.accessToken.token))
                            } else {
                                continuation.resumeWithException(IllegalStateException("Facebook login empty success"))
                            }
                        }

                        override fun onCancel() {
                            continuation.cancel()
                        }

                        override fun onError(error: FacebookException?) {
                            continuation.resumeWithException(
                                error ?: IllegalStateException("Facebook login empty error")
                            )
                        }
                    }
                )
                loginManager.logInWithReadPermissions(fragment, listOf("email"))
            }
        }.also {
            loginManager.unregisterCallback(callbackManager)
        }.getOrThrow()

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        callbackManager.onActivityResult(requestCode, resultCode, data)
}
