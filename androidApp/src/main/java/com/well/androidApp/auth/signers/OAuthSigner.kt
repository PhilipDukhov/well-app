package com.well.androidApp.auth.signers

import android.app.Activity
import com.facebook.FacebookCallback
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.well.androidApp.Callback
import java.lang.Exception

class OAuthSigner(providerURL: String, callback: Callback<AuthCredential, Exception>): SocialSigner(callback) {
    private val provider = OAuthProvider.newBuilder(providerURL)

    override fun requestCredentials(activity: Activity) {
        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener {
                callback.onSuccess(it.credential!!)
            }
            .addOnFailureListener {
                callback.onError(it)
            }
    }

}