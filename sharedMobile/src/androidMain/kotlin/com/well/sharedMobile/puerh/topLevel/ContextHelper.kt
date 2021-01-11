package com.well.sharedMobile.puerh.topLevel

import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.well.utils.Context
import com.well.sharedMobile.utils.ImageContainer
import kotlinx.coroutines.*
import kotlin.coroutines.resume

actual class ContextHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: Alert) =
        AlertDialog.Builder(context.componentActivity)
            .setMessage(alert.description)
            .setPositiveButton(alert.positiveAction)
            .setNegativeButton(alert.negativeAction)
            .create()
            .show()

    private fun AlertDialog.Builder.setPositiveButton(action: Alert.Action) =
        apply {
            setPositiveButton(action.title) { _, _ -> action.handle() }
        }

    private fun AlertDialog.Builder.setNegativeButton(action: Alert.Action) =
        apply {
            setNegativeButton(action.title) { _, _ -> action.handle() }
        }

    private fun Alert.Action.handle() = when (this) {
        Alert.Action.Ok -> Unit
        Alert.Action.Settings ->
            context.componentActivity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts(
                        "package",
                        context.componentActivity.packageName,
                        null
                    )
                }
            )
    }

    actual suspend fun pickSystemImage(): ImageContainer =
        suspendCancellableCoroutine<ImageContainer> { continuation ->
            this.continuation = continuation
            val intents = listOf(
                Intent(ACTION_GET_CONTENT),
                Intent(ACTION_PICK, EXTERNAL_CONTENT_URI),
            ).onEach {
                it.type = "image/*"
            }
            imagePickerLauncher.launch(createChooser(intents))
        }.also {
            continuation = null
        }

    private val imagePickerLauncher = context.componentActivity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val data = it.data?.data
        if (data != null) {
            continuation?.resume(ImageContainer(data, context.componentActivity))
        } else {
            continuation?.cancel(IllegalStateException("imagePickerLauncher result: ${it.resultCode}"))
        }
    }

    private var continuation: CancellableContinuation<ImageContainer>? = null

    private fun createChooser(intents: List<Intent>): Intent {
        val chooserIntent = createChooser(intents.first(), "Select Image")
        chooserIntent.putExtra(
            EXTRA_INITIAL_INTENTS,
            intents.drop(1)
                .toTypedArray()
        )
        return chooserIntent
    }
}