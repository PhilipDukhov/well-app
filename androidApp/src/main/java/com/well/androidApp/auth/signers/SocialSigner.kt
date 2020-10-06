package com.well.androidApp.auth.signers

import android.app.Activity
import android.content.Intent
import com.google.firebase.auth.AuthCredential
import com.well.androidApp.Callback
import java.lang.Exception

abstract class SocialSigner(val callback: Callback<AuthCredential, Exception>) {
    abstract fun requestCredentials(activity: Activity)
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean = false
}