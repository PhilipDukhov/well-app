package com.well.sharedMobile.puerh

import com.well.utils.Context
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.UIKit.*

actual class AlertHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: TopLevelFeature.Alert) {
        MainScope().launch {
            context
                .rootController
                .presentViewController(
                    UIAlertController.alertControllerWithTitle(
                        alert.description,
                        "",
                        UIAlertControllerStyleAlert
                    ).apply {
                        listOf(
                            alert.positiveAction to UIAlertActionStyleCancel,
                            alert.negativeAction to UIAlertActionStyleDefault,
                        ).map { actionAndStyle ->
                            UIAlertAction.actionWithTitle(
                                actionAndStyle.first.title,
                                actionAndStyle.second,
                            ) { actionAndStyle.first.handle() }
                        }.forEach(::addAction)
                    },
                    true
                ) {}
        }
    }

    private fun TopLevelFeature.Alert.Action.handle() {
        when (this) {
            TopLevelFeature.Alert.Action.Ok -> Unit
            TopLevelFeature.Alert.Action.Settings ->
                UIApplication
                    .sharedApplication
                    .openURL(NSURL(string = UIApplicationOpenSettingsURLString))
        }
    }
}