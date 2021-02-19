package com.well.sharedMobile.puerh._topLevel

import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.*
import com.well.utils.map
import com.well.atomic.Closeable
import com.well.atomic.freeze
import kotlinx.coroutines.*
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.collections.map
import kotlin.coroutines.resume

internal actual class ContextHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: Alert) {
        presentViewController(
            UIAlertController.alertControllerWithTitle(
                alert.title,
                alert.description,
                UIAlertControllerStyleAlert
            ).apply {
                listOf(
                    alert.positiveAction to UIAlertActionStyleCancel,
                    alert.negativeAction to UIAlertActionStyleDefault,
                ).mapNotNull { it.mapNotNull(transformA = { it }, transformB = { it }) }
                    .map { actionAndStyle ->
                        UIAlertAction.actionWithTitle(
                            actionAndStyle.first.title,
                            actionAndStyle.second,
                        ) { actionAndStyle.first.handle() }
                    }
                    .forEach(::addAction)
            }
        )
    }

    actual fun showSheet(actions: List<Action>): Closeable {
        val alertController = UIAlertController.alertControllerWithTitle(
            null,
            null,
            UIAlertControllerStyleActionSheet
        ).apply {
            (actions.map { action ->
                UIAlertAction.actionWithTitle(
                    action.title,
                    UIAlertActionStyleDefault,
                ) { action.block() }
            } + UIAlertAction.actionWithTitle(
                "Cancel",
                UIAlertActionStyleCancel,
                null,
            ))
                .forEach(::addAction)
        }
        presentViewController(alertController)
        return object : Closeable {
            override fun close() {
                alertController.dismissViewControllerAnimated(true, null)
            }
        }
    }

    actual fun openUrl(url: String) {
        UIApplication.sharedApplication.openURL(NSURL(string = url))
    }

    private fun presentViewController(
        viewController: UIViewController,
        animated: Boolean = true
    ) {
        context
            .rootController
            .presentViewController(viewController, animated, null)
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
                    .presentViewController(imagePicker, true) {}
            }
        }
}
