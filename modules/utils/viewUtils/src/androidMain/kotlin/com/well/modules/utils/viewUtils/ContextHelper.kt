package com.well.modules.utils.viewUtils

import com.well.modules.atomic.Closeable
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ContextHelper actual constructor(actual val appContext: AppContext): WebAuthenticator {
    actual fun showAlert(alert: Alert) =
        AlertDialog.Builder(appContext.androidContext)
            .setTitle(alert.title)
            .setMessage(alert.description)
            .setPositiveButton(alert.positiveAction)
            .setNegativeButton(alert.negativeAction)
            .create()
            .show()

    actual fun showSheet(actions: List<Action>): Closeable {
        val builder = BottomSheetDialogBuilder(appContext.androidContext)
        actions.forEach(builder::add)
        builder.show()
        return object : Closeable {
            override fun close() {
                MainScope().launch {
                    builder.dismiss()
                }
            }
        }
    }

    actual fun openUrl(url: String) {
        MainScope().launch {
            appContext.androidContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    actual override suspend fun webAuthenticate(
        url: String,
        requestCode: Int
    ): String = suspendCancellableCoroutine {
        MainScope().launch {
            val preferredPackage = getPreferredCustomTabsPackage()
            val intent = if (preferredPackage != null) {
                val customTabsBuilder = CustomTabsIntent.Builder()
                val customTabsIntent = customTabsBuilder.build()
                customTabsIntent.intent.data = Uri.parse(url)
                customTabsIntent.intent.setPackage(preferredPackage)
                customTabsIntent.intent
            } else {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                intent
            }
            @Suppress("DEPRECATION")
            appContext.androidContext.startActivityForResult(intent, requestCode)
        }
    }

    private fun AlertDialog.Builder.setPositiveButton(action: Alert.Action?) =
        apply {
            if (action != null) {
                setPositiveButton(action.title) { _, _ -> action.handle() }
            }
        }

    private fun AlertDialog.Builder.setNegativeButton(action: Alert.Action?) =
        apply {
            if (action != null) {
                setNegativeButton(action.title) { _, _ -> action.handle() }
            }
        }

    private fun Alert.Action.handle() = when (this) {
        Alert.Action.Ok -> Unit
        Alert.Action.Settings ->
            appContext.androidContext.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts(
                        "package",
                        appContext.androidContext.packageName,
                        null
                    )
                }
            )
    }

    actual suspend fun pickSystemImage() =
        suspendCancellableCoroutine<LocalImage> { continuation ->
            this.continuation = continuation
            val intents = listOf(
                Intent(Intent.ACTION_GET_CONTENT),
                Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI),
            ).onEach {
                it.type = "image/*"
            }
            imagePickerLauncher.launch(createChooser(intents))
        }.also {
            continuation = null
        }

    private val imagePickerLauncher = appContext.androidContext.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val data = it.data?.data
        if (data != null) {
            continuation?.resume(LocalImage(data, appContext.androidContext))
        } else {
            continuation?.cancel(IllegalStateException("imagePickerLauncher result: ${it.resultCode}"))
        }
    }

    private var continuation: CancellableContinuation<LocalImage>? = null

    private fun createChooser(intents: List<Intent>): Intent {
        val chooserIntent = Intent.createChooser(intents.first(), "Select Image")
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            intents.drop(1)
                .toTypedArray()
        )
        return chooserIntent
    }

    private fun getDefaultBrowser(): String? {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val packageManager = appContext.androidContext.packageManager
        val resolveInfo = packageManager.resolveActivity(
            browserIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        resolveInfo ?: return null
        return resolveInfo.activityInfo.packageName
    }

    private fun getCustomTabsPackages(): List<String> {
        val packageManager = appContext.androidContext.packageManager
        val activityIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val resolvedActivityList =
            packageManager.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = mutableListOf<String>()
        resolvedActivityList.forEach {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(it.activityInfo.packageName)
            if (packageManager.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(it.activityInfo.packageName)
            }
        }
        return packagesSupportingCustomTabs
    }

    private fun getPreferredCustomTabsPackage(): String? {
        val defaultBrowser = getDefaultBrowser()
        val supportedPackages = getCustomTabsPackages()
        if (supportedPackages.isEmpty()) return null
        val preferredPackage = getCustomTabsPackages()
            .firstOrNull { it == defaultBrowser }
        return preferredPackage ?: supportedPackages[0]
    }
}