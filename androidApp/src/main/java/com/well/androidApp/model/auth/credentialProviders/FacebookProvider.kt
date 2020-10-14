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
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FacebookProvider : CredentialProvider {
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val loginManager = LoginManager.getInstance()

    fun finalize() {
        loginManager.unregisterCallback(callbackManager)
    }

    override suspend fun getCredentials(fragment: Fragment): AuthCredential =
        suspendCoroutine { globalContinuation ->
            val continuation = Continuation<AuthCredential>(globalContinuation.context) {
                loginManager.unregisterCallback(callbackManager)
                globalContinuation.resumeWith(it)
            }
            loginManager.registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        if (result != null) {
                            continuation.resume(FacebookAuthProvider.getCredential(result.accessToken.token))
                        } else {
                            continuation.resumeWithException(Exception("Facebook Empty Success"))
                        }
                    }

                    override fun onCancel() {
                        continuation.resumeWithException(CancellationException("Task $this was cancelled normally."))
                    }

                    override fun onError(error: FacebookException?) {
                        if (error != null) {
                            continuation.resumeWithException(error)
                        }
                    }
                }
            )
            loginManager.logInWithReadPermissions(fragment, listOf("email"))
        }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        callbackManager.onActivityResult(requestCode, resultCode, data)
}
