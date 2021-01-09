package com.well.sharedMobile.puerh.topLevel

import com.well.utils.Context
import com.well.utils.Image
import com.well.utils.freeze
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class ContextHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: Alert) {
        MainScope().launch {
            context
                .rootController
                .presentViewController(
                    UIAlertController.alertControllerWithTitle(
                        alert.description,
                        "",
                        UIAlertControllerStyleAlert
                    )
                        .apply {
                            listOf(
                                alert.positiveAction to UIAlertActionStyleCancel,
                                alert.negativeAction to UIAlertActionStyleDefault,
                            ).map { actionAndStyle ->
                                UIAlertAction.actionWithTitle(
                                    actionAndStyle.first.title,
                                    actionAndStyle.second,
                                ) { actionAndStyle.first.handle() }
                            }
                                .forEach(::addAction)
                        },
                    true
                ) {}
        }
    }

    private fun Alert.Action.handle() {
        when (this) {
            Alert.Action.Ok -> Unit
            Alert.Action.Settings ->
                UIApplication
                    .sharedApplication
                    .openURL(NSURL(string = UIApplicationOpenSettingsURLString))
        }
    }

    actual suspend fun pickSystemImage(): Image =
        suspendCancellableCoroutine { continuation ->
            MainScope().launch {
                val imagePicker = UIImagePickerController()
                imagePicker.delegate = object : NSObject(), UINavigationControllerDelegateProtocol,
                    UIImagePickerControllerDelegateProtocol {
                    override fun imagePickerController(
                        picker: UIImagePickerController,
                        didFinishPickingMediaWithInfo: Map<Any?, *>
                    ) {
                        val url = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as NSURL
                        continuation.resume(Image(url.absoluteString!!))
                    }

                    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                        continuation.cancel()
                    }
                }.freeze()
                context
                    .rootController
                    .presentViewController(imagePicker, true) {}
            }
        }
}
