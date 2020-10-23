package com.well.androidApp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import by.kirich1409.viewbindingdelegate.viewBinding
import com.well.androidApp.R
import com.well.androidApp.databinding.FragmentSignInStartBinding
import com.well.androidApp.model.auth.SocialNetwork
import com.well.androidApp.model.auth.SocialNetwork.*
import com.well.androidApp.model.auth.SocialNetworkService
import com.well.androidApp.ui.MainActivity
import com.well.androidApp.ui.customViews.BaseFragment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SignInStartFragment : BaseFragment(R.layout.fragment_sign_in_start) {
    private lateinit var socialNetworkService: SocialNetworkService
    private val viewBinding: FragmentSignInStartBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialNetworkService = SocialNetworkService(requireContext())
        SocialNetwork.values().forEach { socialNetwork ->
            viewBinding.run {
                when (socialNetwork) {
                    Apple -> authApple
                    Twitter -> authTwitter
                    Facebook -> authFacebook
                    Google -> authGoogle
                }
            }.setOnClickListener {
                onSocialNetworkButtonClick(socialNetwork)
            }
        }
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
