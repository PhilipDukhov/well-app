package com.well.androidApp.ui

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.github.aakira.napier.BuildConfig.DEBUG
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.firebase.auth.FirebaseAuth
import com.well.androidApp.CrashlyticsAntilog
import com.well.androidApp.R
import com.well.androidApp.ui.MainActivity.State.*

class MainActivity : AppCompatActivity() {
    enum class State {
        Idle, Processing
    }

    var state = Idle
        set(value) {
            if (value == field) {
                return
            }
            field = value
            findViewById<View>(R.id.progress_overlay)?.visibility = when (value) {
                Idle -> GONE
                Processing -> VISIBLE
            }
        }
    private val auth = FirebaseAuth.getInstance()

    private var signedIn = false
        set(value) {
            if (value == field) {
                return
            }
            field = value
            updateNavigationHost()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeLogging()

        auth.addAuthStateListener {
            signedIn = it.currentUser != null
        }
    }

    private fun updateNavigationHost() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.graph.id
        val host = NavHostFragment.create(
            if (signedIn) {
                R.navigation.navigation_main_graph
            } else {
                R.navigation.navigation_sign_in_graph
            }
        )
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, host)
            .setPrimaryNavigationFragment(host)
            .commit()
        state = Idle
    }

    private fun initializeLogging() =
        Napier.base(if (DEBUG) DebugAntilog() else CrashlyticsAntilog(this))
}
