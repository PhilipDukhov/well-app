package com.well.sharedMobile.puerh

import com.well.utils.Context
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature

expect class AlertHelper(context: Context) {
    val context: Context
    fun showAlert(alert: TopLevelFeature.Alert)
}