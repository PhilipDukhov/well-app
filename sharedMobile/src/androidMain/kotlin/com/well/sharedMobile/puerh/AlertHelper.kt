package com.well.sharedMobile.puerh

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.well.utils.Context
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature

actual class AlertHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: TopLevelFeature.Alert) =
        AlertDialog.Builder(context.componentActivity)
            .setMessage(alert.description)
            .setPositiveButton(alert.positiveAction)
            .setNegativeButton(alert.negativeAction)
            .create()
            .show()

    private fun AlertDialog.Builder.setPositiveButton(action: TopLevelFeature.Alert.Action) =
        apply {
            setPositiveButton(action.title) { _, _ -> action.handle() }
        }

    private fun AlertDialog.Builder.setNegativeButton(action: TopLevelFeature.Alert.Action) =
        apply {
            setNegativeButton(action.title) { _, _ -> action.handle() }
        }

    private fun TopLevelFeature.Alert.Action.handle() = when (this) {
        TopLevelFeature.Alert.Action.Ok -> Unit
        TopLevelFeature.Alert.Action.Settings ->
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
}