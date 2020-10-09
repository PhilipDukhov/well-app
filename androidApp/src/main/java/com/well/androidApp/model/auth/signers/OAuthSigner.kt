package com.well.androidApp.model.auth.signers

import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.well.androidApp.Callback

class OAuthSigner(providerURL: String, callback: Callback<AuthCredential, Exception>) :
    SocialSigner(callback) {
    private val provider = OAuthProvider.newBuilder(providerURL)

    override fun requestCredentials(fragment: Fragment) {
        fragment.activity?.let { activity ->
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
}