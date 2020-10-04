package com.well.androidApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var facebookCallbackManager: CallbackManager

    enum class Request(val code: Int) {
        GoogleAuth(9001)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        initializeFacebook()
        initializeGoogle()

        findViewById<Button>(R.id.auth_google).setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, Request.GoogleAuth.code)
        }
        findViewById<Button>(R.id.auth_facebook).setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
        }
        findViewById<Button>(R.id.auth_twitter).setOnClickListener {
            val provider = OAuthProvider.newBuilder("twitter.com")
            FirebaseAuth.getInstance()
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    val cred = it.credential!!
                    println("twitter success $cred")
                }
                .addOnFailureListener {
                    println("twitter error $it")
                }
        }
        val pendingResultTask = FirebaseAuth.getInstance().pendingAuthResult
        pendingResultTask?.addOnSuccessListener {
            val cred = it.credential!!
            print("1 twitter success $cred")
        }
            ?.addOnFailureListener {
                print("1 twitter error $it")
            }
    }

    private fun initializeFacebook() {
        facebookCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(
            facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    val credential = FacebookAuthProvider.getCredential(result!!.accessToken.token)
                    println("success $credential")
                }

                override fun onCancel() {
                    println("cancel")
                }

                override fun onError(error: FacebookException?) {
                    println("facebook error $error")
                }
            })
    }

    private fun initializeGoogle() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        googleSignInClient.signOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Request.GoogleAuth.code) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                println("Google sign in failed $e")
                // ...
            }
        }

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        println("Google sign in succeed ${GoogleAuthProvider.getCredential(idToken, null)}")
    }
}
