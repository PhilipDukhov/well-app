package com.well.androidApp.model.auth

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.well.androidApp.Callback
import com.well.androidApp.model.auth.signers.FacebookSigner
import com.well.androidApp.model.auth.signers.GoogleSigner
import com.well.androidApp.model.auth.signers.OAuthSigner
import com.well.androidApp.model.auth.signers.SocialSigner

class SocialNetworkService(
    private val context: Context,
    private val callback: Callback<AuthCredential, Exception>
) {
    private val singers = mutableMapOf<SocialNetwork, SocialSigner>()

    init {
        FirebaseAuth.getInstance().pendingAuthResult
            ?.addOnSuccessListener {
                val cred = it.credential!!
                print("1 twitter success $cred")
            }
            ?.addOnFailureListener {
                print("1 twitter error $it")
            }
    }

    fun requestCredentials(
        network: SocialNetwork,
        fragment: Fragment
    ) {
        singers.getOrPut(network) {
            when (network) {
                SocialNetwork.Google -> GoogleSigner(context, callback)
                SocialNetwork.Apple -> TODO()
                SocialNetwork.Twitter -> OAuthSigner("twitter.com", callback)
                SocialNetwork.Facebook -> FacebookSigner(callback)
            }
        }.requestCredentials(fragment)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        singers.values.first { it.handleActivityResult(requestCode, resultCode, data) }
    }
}