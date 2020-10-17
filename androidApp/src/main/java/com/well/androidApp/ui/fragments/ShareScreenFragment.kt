package com.well.androidApp.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.well.androidApp.R
import com.well.shared.FirebaseManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ShareScreenFragment : Fragment() {
    private lateinit var imageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_share_screen, container, false).apply {
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
    }!!

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
            Intent(Intent.ACTION_GET_CONTENT),
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
        intents.forEach {
            it.type = "image/*"
        }

        val chooserIntent = Intent.createChooser(intents.first(), "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.drop(1).toTypedArray())
        startActivityForResult(chooserIntent, pickImageRequestCode)
    }

    private fun updateImage(uri: Uri) {
        Glide.with(requireContext()).apply {
            load(uri)
                .into(imageView)
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
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
}
