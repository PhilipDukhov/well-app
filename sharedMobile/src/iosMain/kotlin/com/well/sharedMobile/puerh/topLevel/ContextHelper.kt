package com.well.sharedMobile.puerh.topLevel

import com.well.utils.Context
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.freeze
import kotlinx.coroutines.*
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    actual suspend fun pickSystemImage(): ImageContainer =
        Dispatchers.Main {
            suspendCancellableCoroutine { continuation ->
                val imagePicker = UIImagePickerController()
                @Suppress("NOTHING_TO_OVERRIDE")
                imagePicker.delegate = object : NSObject(),
                    UINavigationControllerDelegateProtocol,
                    UIImagePickerControllerDelegateProtocol {

                    override fun imagePickerController(
                        picker: UIImagePickerController,
                        didFinishPickingMediaWithInfo: Map<Any?, *>
                    ) {
                        val url = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as NSURL
                        picker.dismissViewControllerAnimated(true) {}
                        continuation.resume(ImageContainer(url.path!!))
                    }

                    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                        continuation.cancel()
                        picker.dismissViewControllerAnimated(true) {}
                    }
                }.freeze()
                context
                    .rootController
                    .presentViewController(imagePicker, true) {
                        NSLog("presentViewController finished")
                    }
            }
        }
}
