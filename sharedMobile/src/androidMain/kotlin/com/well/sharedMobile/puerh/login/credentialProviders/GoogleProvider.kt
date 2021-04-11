package com.well.sharedMobile.puerh.login.credentialProviders

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS
import com.google.android.gms.common.api.ApiException
import com.well.modules.utils.Context
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleProvider(private val context: Context, tokenId: String) : CredentialProvider(context) {
    private val authRequestCode = 9001
    private val googleSignInClient: GoogleSignInClient
    private var continuation: CancellableContinuation<AuthCredential>? = null

    init {
        val googleSignInOptions = GoogleSignInOptions
            .Builder(DEFAULT_SIGN_IN)
            .requestIdToken(tokenId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context.componentActivity, googleSignInOptions)
    }

    override suspend fun getCredentials(): AuthCredential {
        googleSignInClient.signOut().await()
        return suspendCancellableCoroutine {
            continuation = it
            context.componentActivity.startActivityForResult(
                googleSignInClient.signInIntent,
                authRequestCode
            )
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val continuation = continuation
        if (continuation == null || requestCode != authRequestCode) {
            return false
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            continuation.resume(AuthCredential.GoogleCredential(account.idToken!!))
        } catch (t: Throwable) {
            when ((t as? ApiException)?.statusCode) {
                SIGN_IN_CANCELLED -> continuation.cancel()
                SIGN_IN_CURRENTLY_IN_PROGRESS -> return false
                else -> continuation.resumeWithException(t)
            }
        }
        this.continuation = null
        return true
    }
}
