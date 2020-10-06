package com.well.androidApp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.aakira.napier.BuildConfig
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.firebase.auth.AuthCredential
import com.well.androidApp.Callback
import com.well.androidApp.CrashlyticsAntilog
import com.well.androidApp.R
import com.well.androidApp.ui.auth.SocialNetwork
import com.well.androidApp.ui.auth.SocialNetworkService


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

        initializeLogging()

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

    private fun initializeLogging() =
        Napier.base(if (BuildConfig.DEBUG) DebugAntilog() else CrashlyticsAntilog(this))
}
