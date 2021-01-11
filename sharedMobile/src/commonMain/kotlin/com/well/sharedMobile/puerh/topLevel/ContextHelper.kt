package com.well.sharedMobile.puerh.topLevel

import com.well.utils.Context
import com.well.sharedMobile.utils.ImageContainer

expect class ContextHelper(context: Context) {
    val context: Context
    fun showAlert(alert: Alert)
    suspend fun pickSystemImage(): ImageContainer
}                           