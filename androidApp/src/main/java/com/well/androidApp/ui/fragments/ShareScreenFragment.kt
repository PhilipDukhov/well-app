package com.well.androidApp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.well.androidApp.R

class ShareScreenFragment : Fragment() {
    private val FirebaseUser.description: String
        get() { return "$displayName\n$email\n${providerData.map { it.providerId }.filter { it != "firebase" }.joinToString("\n")}" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_screen, container, false).apply {
            findViewById<Button>(R.id.button).setOnClickListener { FirebaseAuth.getInstance().signOut() }
            findViewById<TextView>(R.id.textView).apply {
                text = FirebaseAuth.getInstance().currentUser?.description
            }
        }
    }
}