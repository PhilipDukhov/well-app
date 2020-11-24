package com.well.auth.credentialProviders

import cocoapods.FBSDKCoreKit.FBSDKApplicationDelegate
import cocoapods.FBSDKLoginKit.FBSDKLoginManager
import com.well.auth.Context
import com.well.auth.LoginView
import com.well.auth.credentialProviders.AuthCredential.FacebookCredential
import com.well.auth.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class FacebookProvider actual constructor(context: Context) : CredentialProvider(context) {
    private val loginManager = FBSDKLoginManager()

    init {
        context.apply {
            FBSDKApplicationDelegate.sharedInstance.application(
                application,
                launchOptions
            )
        }
    }

    actual override suspend fun getCredentials(loginView: LoginView): AuthCredential =
        suspendCancellableCoroutine { continuation ->
            loginManager.logInWithPermissions(
                listOf<String>(), loginView
            ) { result, error ->
                val token = result?.token?.tokenString()
                when {
                    error != null -> continuation.resumeWithException(error)
                    result?.isCancelled == true -> continuation.cancel()
                    token != null -> continuation.resume(FacebookCredential(token))
                    else -> continuation.resumeWithException(IllegalStateException())
                }
            }
        }

    override fun application(app: UIApplication, openURL: NSURL, options: Map<Any?, *>): Boolean =
        FBSDKApplicationDelegate.sharedInstance.application(
            app,
            openURL,
            options
        )
}