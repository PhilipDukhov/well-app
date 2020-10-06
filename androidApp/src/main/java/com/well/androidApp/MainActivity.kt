package com.well.androidApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.AuthCredential
import com.well.androidApp.auth.SocialNetwork
import com.well.androidApp.auth.SocialNetworkService


class MainActivity : AppCompatActivity() {
    private lateinit var socialNetworkService: SocialNetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        socialNetworkService = SocialNetworkService(this,
            object : Callback<AuthCredential, java.lang.Exception> {
                override fun onSuccess(result: AuthCredential) {
                    print(result)
                }

                override fun onCancel() {
                    print("canceled")
                }

                override fun onError(error: Exception) {
                    print(error)
                }
            })

        findViewById<Button>(R.id.auth_apple).setOnClickListener {
            socialNetworkService.requestCredentials(SocialNetwork.Apple, this)
        }
        findViewById<Button>(R.id.auth_google).setOnClickListener {
            socialNetworkService.requestCredentials(SocialNetwork.Google, this)
        }
        findViewById<Button>(R.id.auth_facebook).setOnClickListener {
            socialNetworkService.requestCredentials(SocialNetwork.Facebook, this)
        }
        findViewById<Button>(R.id.auth_twitter).setOnClickListener {
            socialNetworkService.requestCredentials(SocialNetwork.Twitter, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialNetworkService.handleActivityResult(requestCode, resultCode, data)
    }
}
