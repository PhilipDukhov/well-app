package com.well.shared.puerh

import com.well.shared.puerh.featureProvider.Context
import com.well.shared.puerh.topLevel.TopLevelFeature

expect class AlertHelper(context: Context) {
    val context: Context
    fun showAlert(alert: TopLevelFeature.Alert)
}