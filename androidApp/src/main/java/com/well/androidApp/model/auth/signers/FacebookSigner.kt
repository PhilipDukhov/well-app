package com.well.androidApp.model.auth.signers

import android.content.Intent
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.well.androidApp.Callback

class FacebookSigner(callback: Callback<AuthCredential, Exception>) : SocialSigner(callback) {
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val loginManager = LoginManager.getInstance()

    init {
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    if (result != null) {
                        callback.onSuccess(FacebookAuthProvider.getCredential(result.accessToken.token))
                    } else {
                        callback.onError(Exception("Facebook Empty Success"))
                    }
                }

                override fun onCancel() {
                    callback.onCancel()
                }

                override fun onError(error: FacebookException?) {
                    if (error != null) {
                        callback.onError(error)
                    }
                }
            })
    }

    fun finalize() {
        loginManager.unregisterCallback(callbackManager)
    }

    override fun requestCredentials(fragment: Fragment) {
        loginManager.logInWithReadPermissions(fragment, listOf("email"))
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}