package com.well.androidApp.model.auth.credentialProviders

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.well.androidApp.R
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleProvider(context: Context) : CredentialProvider {
    private val authRequestCode = 9001
    private val googleSignInClient: GoogleSignInClient
    private var continuation: Continuation<AuthCredential>? = null

    init {
        val googleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    }

    override suspend fun getCredentials(fragment: Fragment): AuthCredential =
        suspendCoroutine {
            continuation = it
            googleSignInClient.signOut()
                .addOnCompleteListener {
                    fragment.startActivityForResult(
                        googleSignInClient.signInIntent,
                        authRequestCode
                    )
                }
        }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != authRequestCode) {
            return false
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            continuation?.resume(GoogleAuthProvider.getCredential(account.idToken!!, null))
        } catch (e: ApiException) {
            continuation?.resumeWithException(e)
        }
        continuation = null
        return true
    }
}
