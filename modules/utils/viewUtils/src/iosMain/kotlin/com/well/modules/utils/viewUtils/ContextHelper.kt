package com.well.modules.utils.viewUtils

import com.well.modules.atomic.Closeable
import com.well.modules.atomic.freeze
import com.well.modules.models.NetworkConstants
import com.well.modules.utils.kotlinUtils.mapNotNull
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorCodeCanceledLogin
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.Foundation.NSURL
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class ContextHelper actual constructor(actual val appContext: AppContext) :
    WebAuthenticator {
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
        MainScope().launch {
            UIApplication.sharedApplication.openURL(NSURL(string = url))
        }
    }

    actual override suspend fun webAuthenticate(url: String, requestCode: Int): String {
        return suspendCancellableCoroutine { continuation ->
            MainScope().launch {
                val session = ASWebAuthenticationSession(
                    uRL = NSURL(string = url),
                    callbackURLScheme = NetworkConstants.oauthCallbackProtocol,
                    completionHandler = completionHandler@{ callbackUrl, error ->
                        if (continuation.isCancelled) {
                            return@completionHandler
                        }
                        if (
                            error?.domain == ASWebAuthenticationSessionErrorDomain
                            && error?.code == ASWebAuthenticationSessionErrorCodeCanceledLogin
                        ) {
                            continuation.cancel()
                        } else if (error != null) {
                            continuation.resumeWithException(error)
                        } else {
                            continuation.resume(callbackUrl!!.absoluteString!!)
                        }
                    }
                )
                session.presentationContextProvider =
                    appContext.rootController as? ASWebAuthenticationPresentationContextProvidingProtocol
                session.start()
                continuation.invokeOnCancellation {
                    session.cancel()
                }
            }
        }
    }

    private fun presentViewController(
        viewController: UIViewController,
        animated: Boolean = true
    ) {
        MainScope().launch {
            topmostController().presentViewController(viewController, animated, null)
        }
    }

    private fun topmostController(): UIViewController {
        var topmost = appContext.rootController
        while (topmost.presentedViewController != null && !topmost.presentedViewController!!.isBeingDismissed()) {
            topmost = topmost.presentedViewController!!
        }
        return topmost
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

    actual suspend fun pickSystemImage(): LocalImage =
        withContext(Dispatchers.Main) {
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
                        val url =
                            didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as NSURL
                        picker.dismissViewControllerAnimated(true) {}
                        continuation.resume(LocalImage(url.path!!))
                    }

                    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                        continuation.cancel()
                        picker.dismissViewControllerAnimated(true) {}
                    }
                }.freeze()
                presentViewController(imagePicker)
            }
        }
}