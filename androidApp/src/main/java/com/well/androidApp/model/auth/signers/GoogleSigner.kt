package com.well.androidApp.model.auth.signers

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.well.androidApp.Callback
import com.well.androidApp.R

class GoogleSigner(context: Context, callback: Callback<AuthCredential, Exception>) :
    SocialSigner(callback) {
    private val authRequestCode = 9001
    private val googleSignInClient: GoogleSignInClient

    init {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    }

    override fun requestCredentials(fragment: Fragment) {
        googleSignInClient.signOut()
        fragment.activity?.startActivityForResult(googleSignInClient.signInIntent, authRequestCode)
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != authRequestCode) return false
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            callback.onSuccess(GoogleAuthProvider.getCredential(account.idToken!!, null))
        } catch (e: ApiException) {
            callback.onError(e)
        }
        return true
    }
}