package com.well.androidApp.model.auth.credentialProviders

import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthCredential

interface CredentialProvider {
    suspend fun getCredentials(fragment: Fragment): AuthCredential
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}