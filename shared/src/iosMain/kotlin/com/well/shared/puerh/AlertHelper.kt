package com.well.shared.puerh

import com.well.shared.puerh.featureProvider.Context
import com.well.shared.puerh.topLevel.TopLevelFeature

actual class AlertHelper actual constructor(actual val context: Context) {
    actual fun showAlert(alert: TopLevelFeature.Alert) {
    }
}