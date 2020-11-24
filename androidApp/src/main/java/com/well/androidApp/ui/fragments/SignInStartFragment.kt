package com.well.androidApp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.well.androidApp.R
import com.well.androidApp.databinding.FragmentSignInStartBinding
import com.well.androidApp.ui.MainActivity
import com.well.androidApp.ui.customViews.BaseFragment
import com.well.auth.Context
import com.well.auth.SocialNetwork
import com.well.auth.SocialNetwork.*
import com.well.auth.SocialNetworkService
import com.well.auth.handleActivityResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SignInStartFragment : BaseFragment(R.layout.fragment_sign_in_start) {
    private lateinit var socialNetworkService: SocialNetworkService

    private lateinit var viewBinding: FragmentSignInStartBinding  // by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSignInStartBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialNetworkService = SocialNetworkService(Context(requireContext()))
        SocialNetwork.values().forEach { socialNetwork ->
            viewBinding.run {
                when (socialNetwork) {
// Apple -> authApple
// Twitter -> authTwitter
                    Facebook -> authFacebook
                    Google -> authGoogle
                }
            }.setOnClickListener {
                onSocialNetworkButtonClick(socialNetwork)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        socialNetworkService.handleActivityResult(requestCode, resultCode, data)
    }

    private fun onSocialNetworkButtonClick(socialNetwork: SocialNetwork) {
        activity?.state = MainActivity.State.Processing
        GlobalScope.launch {
            try {
                val result = socialNetworkService.login(socialNetwork, this@SignInStartFragment)
                println("yai $result")
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
