package com.well.auth.credentialProviders

import cocoapods.GoogleSignIn.*
import com.well.auth.Context
import com.well.auth.LoginView
import com.well.utils.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class GoogleProvider actual constructor(context: Context) : CredentialProvider(context) {
    private val signIn = GIDSignIn.sharedInstance()!!.apply {
        clientID = "567261840338-fbpj843t6o0lkscvafv7l4368es1h5ru.apps.googleusercontent.com"
    }

    actual override suspend fun getCredentials(loginView: LoginView): AuthCredential =
        suspendCancellableCoroutine { continuation ->
            signIn.presentingViewController = loginView
            signIn.delegate = object : NSObject(), GIDSignInDelegateProtocol {
                override fun signIn(
                    signIn: GIDSignIn?,
                    didSignInForUser: GIDGoogleUser?,
                    withError: NSError?
                ) {
                    signIn?.delegate = null
                    val token = didSignInForUser?.authentication?.idToken
                    when {
                        withError?.isGoogleSignInCanceled == true -> continuation.cancel()
                        withError != null -> continuation.resumeWithException(withError)
                        token != null -> continuation.resume(AuthCredential.GoogleCredential(token))
                        else -> continuation.resumeWithException(IllegalStateException())
                    }
                }

                private val NSError.isGoogleSignInCanceled
                    get() = domain == kGIDSignInErrorDomain && code == kGIDSignInErrorCodeCanceled
            }
            signIn.signIn()
        }

    override fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>): Boolean =
        signIn.handleURL(openURL)
}