package com.well.androidApp.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.*
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.well.androidApp.databinding.FragmentShareScreenBinding
import com.well.shared.FirebaseManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ShareScreenFragment : Fragment() {
    private lateinit var binding: FragmentShareScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShareScreenBinding.inflate(inflater, container, false)
        binding.signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
        binding.chooseImage.setOnClickListener { onChooseImageClick() }
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            binding.textView.apply {
                text = auth.currentUser?.description
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            pickImageRequestCode, RESULT_OK -> data?.data?.let(::updateImage)

            else -> return
        }
    }

    private val pickImageRequestCode = 7230
    private fun onChooseImageClick() {
        val intents = listOf(
            Intent(ACTION_GET_CONTENT),
            Intent(ACTION_PICK, EXTERNAL_CONTENT_URI),
        )
        intents.forEach {
            it.type = "image/*"
        }

        startActivityForResult(createChooser(intents), pickImageRequestCode)
    }

    private fun updateImage(uri: Uri) {
        Glide.with(requireContext()).apply {
            load(uri)
                .into(binding.imageView)
            asBitmap()
                .load(uri)
                .override(1125, 2436)
                .fitCenter()
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean = false

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let {
                            println("image ${it.width} ${it.height}")
                            GlobalScope.launch {
                                val fileExt: String =
                                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                                upload(fileExt, it)
                            }
                        }
                        return true
                    }
                })
                .submit()
        }
    }

    private suspend fun upload(fileExt: String, bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(JPEG, 100, stream)
        val path = FirebaseManager.manager.upload(stream.toByteArray(), "mountains.$fileExt")
        println("uploaded $path")
    }

    private val FirebaseUser.description: String
        get() {
            return "$displayName\n$email\n${
                providerData
                    .map { it.providerId }
                    .filter { it != "firebase" }
                    .joinToString("\n")
            }"
        }

    private fun createChooser(intents: List<Intent>): Intent {
        val chooserIntent = createChooser(intents.first(), "Select Image")
        chooserIntent.putExtra(EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
        return chooserIntent
    }
}
