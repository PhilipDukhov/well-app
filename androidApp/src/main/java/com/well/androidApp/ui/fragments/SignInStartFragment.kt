package com.well.androidApp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.well.androidApp.R
import com.well.androidApp.model.auth.SocialNetwork
import com.well.androidApp.model.auth.SocialNetworkService
import com.well.androidApp.ui.MainActivity
import com.well.androidApp.ui.customViews.BaseFragment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SignInStartFragment : BaseFragment() {
    private lateinit var socialNetworkService: SocialNetworkService

    private val SocialNetwork.layoutButtonId: Int
        get() = when (this) {
            SocialNetwork.Apple -> R.id.auth_apple
            SocialNetwork.Twitter -> R.id.auth_twitter
            SocialNetwork.Facebook -> R.id.auth_facebook
            SocialNetwork.Google -> R.id.auth_google
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_sign_in_start, container, false).also { view ->
        SocialNetwork.values().forEach { socialNetwork ->
            view.findViewById<Button>(socialNetwork.layoutButtonId).setOnClickListener {
                onSocialNetworkButtonClick(socialNetwork)
            }
        }
    }!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialNetworkService = SocialNetworkService(requireContext())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialNetworkService.handleActivityResult(requestCode, resultCode, data)
    }

    private fun onSocialNetworkButtonClick(socialNetwork: SocialNetwork) {
        activity?.state = MainActivity.State.Processing
        GlobalScope.launch {
            try {
                socialNetworkService.login(socialNetwork, this@SignInStartFragment)
                println("logged in")
            } catch (e: CancellationException) {
            } catch (e: Exception) {
                MainScope().launch {
                    AlertDialog
                        .Builder(requireContext())
                        .setTitle("Login error")
                        .setMessage(e.localizedMessage)
                        .setNeutralButton("OK", null)
                        .show()
                }
            } finally {
                MainScope().launch {
                    activity?.state = MainActivity.State.Idle
                }
            }
        }
    }
}
