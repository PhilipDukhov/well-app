package com.well.androidApp.model.auth.signers

import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthCredential
import com.well.androidApp.Callback

abstract class SocialSigner(val callback: Callback<AuthCredential, Exception>) {
    abstract fun requestCredentials(fragment: Fragment)
    open fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean = false
}