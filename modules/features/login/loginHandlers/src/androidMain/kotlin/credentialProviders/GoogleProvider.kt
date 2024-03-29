package com.well.modules.features.login.loginHandlers.credentialProviders

import com.well.modules.features.login.loginFeature.credentialProvider.AuthCredential
import com.well.modules.utils.viewUtils.AndroidRequestCodes
import com.well.modules.utils.viewUtils.SystemContext
import com.well.sharedMobile.BuildKonfig
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleProvider(private val systemContext: SystemContext) : CredentialProvider(systemContext) {
    private val authRequestCode = AndroidRequestCodes.GoogleProvider.code
    private val googleSignInClient: GoogleSignInClient
    private var continuation: CancellableContinuation<AuthCredential>? = null

    init {
        val googleSignInOptions = GoogleSignInOptions
            .Builder(DEFAULT_SIGN_IN)
            .requestIdToken(BuildKonfig.google_web_client_id)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(systemContext.activity, googleSignInOptions)
    }

    override suspend fun getCredentials(): AuthCredential {
        googleSignInClient.signOut().await()
        return suspendCancellableCoroutine {
            continuation = it
            @Suppress("DEPRECATION")
            systemContext.activity.startActivityForResult(
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
        } catch (e: Exception) {
            when ((e as? ApiException)?.statusCode) {
                SIGN_IN_CANCELLED -> continuation.cancel()
                SIGN_IN_CURRENTLY_IN_PROGRESS -> return false
                else -> continuation.resumeWithException(e)
            }
        }
        this.continuation = null
        return true
    }
}