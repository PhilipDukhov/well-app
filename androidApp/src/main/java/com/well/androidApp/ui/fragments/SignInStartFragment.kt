package com.well.androidApp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

class SignInStartFragment : BaseFragment() {
    private lateinit var socialNetworkService: SocialNetworkService
    private lateinit var binding: FragmentSignInStartBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInStartBinding.inflate(inflater, container, false)
        SocialNetwork.values().forEach { socialNetwork ->
            when (socialNetwork) {
                Apple -> binding.authApple
                Twitter -> binding.authTwitter
                Facebook -> binding.authFacebook
                Google -> binding.authGoogle
            }.setOnClickListener {
                onSocialNetworkButtonClick(socialNetwork)
            }
        }
        return binding.root
    }

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
