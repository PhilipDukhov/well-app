package com.well.sharedMobile.puerh._topLevel

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import com.well.modules.utils.Context
import com.well.sharedMobile.utils.ImageContainer
import com.well.modules.atomic.Closeable
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import androidx.browser.customtabs.CustomTabsIntent
import android.content.pm.ResolveInfo
import java.lang.Exception
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION

import android.content.pm.PackageManager
import android.preference.PreferenceManager
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat

internal actual class ContextHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: Alert) =
        AlertDialog.Builder(context.componentActivity)
            .setTitle(alert.title)
            .setMessage(alert.description)
            .setPositiveButton(alert.positiveAction)
            .setNegativeButton(alert.negativeAction)
            .create()
            .show()

    actual fun showSheet(actions: List<Action>): Closeable {
        val builder = BottomSheetDialogBuilder(context.componentActivity)
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
            context.componentActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    actual suspend fun webAuthenticate(
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
            context.componentActivity.startActivityForResult(intent, requestCode)
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
                Intent(Intent.ACTION_GET_CONTENT),
                Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI),
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
        val packageManager = context.componentActivity.packageManager
        val resolveInfo = packageManager.resolveActivity(
            browserIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        resolveInfo ?: return null
        return resolveInfo.activityInfo.packageName
    }

    private fun getCustomTabsPackages(): List<String> {
        val packageManager = context.componentActivity.packageManager
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