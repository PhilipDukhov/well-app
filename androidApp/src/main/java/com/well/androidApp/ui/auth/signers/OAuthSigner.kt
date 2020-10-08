package com.well.androidApp.ui.auth.signers

import android.app.Activity
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.well.androidApp.Callback

class OAuthSigner(providerURL: String, callback: Callback<AuthCredential, Exception>) :
    SocialSigner(callback) {
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