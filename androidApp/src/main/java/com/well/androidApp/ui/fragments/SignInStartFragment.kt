package com.well.androidApp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.well.androidApp.Callback
import com.well.androidApp.R
import com.well.androidApp.model.auth.SocialNetwork
import com.well.androidApp.model.auth.SocialNetworkService

class SignInStartFragment : Fragment() {
    private lateinit var socialNetworkService: SocialNetworkService

    private val SocialNetwork.layoutButtonId: Int
        get() = when (this) {
            SocialNetwork.Apple -> R.id.auth_apple
            SocialNetwork.Twitter -> R.id.auth_twitter
            SocialNetwork.Facebook -> R.id.auth_facebook
            SocialNetwork.Google -> R.id.auth_google
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in_start, container, false).also { view ->
            SocialNetwork.values().forEach { socialNetwork ->
                view.findViewById<Button>(socialNetwork.layoutButtonId).setOnClickListener {
                    socialNetworkService.requestCredentials(socialNetwork, this)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socialNetworkService = SocialNetworkService(requireContext(),
            object : Callback<AuthCredential, java.lang.Exception> {
                override fun onSuccess(result: AuthCredential) {
                    val progressView = activity?.findViewById<View>(R.id.progress_overlay)
                    progressView?.visibility = View.VISIBLE
                    FirebaseAuth.getInstance()
                        .signInWithCredential(result)
                        .addOnCompleteListener {
                            progressView?.visibility = View.GONE
                        }
                }

                override fun onCancel() {
                    print("canceled")
                }

                override fun onError(error: Exception) {
                    print(error)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        socialNetworkService.handleActivityResult(requestCode, resultCode, data)
    }
}