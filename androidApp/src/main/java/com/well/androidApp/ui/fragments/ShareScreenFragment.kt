package com.well.androidApp.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.well.androidApp.R


class ShareScreenFragment : Fragment() {
    private val FirebaseUser.description: String
        get() {
            return "$displayName\n$email\n${
                providerData.map { it.providerId }.filter { it != "firebase" }.joinToString(
                    "\n"
                )
            }"
        }
    private val processingThread = Thread()
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share_screen, container, false).apply {
            findViewById<Button>(R.id.signOut).setOnClickListener {
                FirebaseAuth.getInstance().signOut()
            }
            findViewById<Button>(R.id.chooseImage).setOnClickListener { onChooseImageClick() }
            imageView = findViewById(R.id.imageView)

            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                findViewById<TextView>(R.id.textView).apply {
                    text = auth.currentUser?.description
                }
            }
        }
    }

    private val pickImageRequestCode = 1234
    private fun onChooseImageClick() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, pickImageRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            pickImageRequestCode, RESULT_OK -> data?.data?.let(::updateImage)
        }
    }

    private fun updateImage(imageUri: Uri) {
        processingThread.run {
            val bitmap = imageUri.getBitmap()
            activity?.runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun Uri.getBitmap(): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    requireContext().contentResolver,
                    this@getBitmap
                )
            )
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, this@getBitmap)
        }
}