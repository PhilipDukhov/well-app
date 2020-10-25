package com.well.androidApp.ui

import android.os.Bundle
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
import com.well.androidApp.databinding.ActivityMainBinding
import com.well.androidApp.ui.MainActivity.State.Idle
import com.well.androidApp.ui.MainActivity.State.Processing

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    enum class State {
        Idle, Processing
    }

    var state = Idle
        set(value) {
            val oldValue = field
            field = value
            stateUpdated(oldValue)
        }

    private val auth = FirebaseAuth.getInstance()
    private lateinit var viewBinding: ActivityMainBinding// by viewBinding()
    private lateinit var navHostFragment: NavHostFragment

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
        initializeLogging()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        setContentView(viewBinding.root)
        updateNavigationHost()
        auth.addAuthStateListener {
            signedIn = it.currentUser != null
            println("logged in ${it.currentUser?.uid}")
        }
    }

    private fun updateNavigationHost() {
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
            .replace(R.id.navHostFragment, host)
            .setPrimaryNavigationFragment(host)
            .commit()
        state = Idle
    }

    private fun stateUpdated(oldValue: State) {
        if (oldValue == state) return
        viewBinding.progressOverlay.visibility = when (state) {
            Idle -> GONE
            Processing -> VISIBLE
        }
    }

    private fun initializeLogging() =
        Napier.base(if (DEBUG) DebugAntilog() else CrashlyticsAntilog(this))
}
