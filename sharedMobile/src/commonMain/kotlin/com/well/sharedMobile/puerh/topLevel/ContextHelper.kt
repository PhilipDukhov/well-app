package com.well.sharedMobile.puerh.topLevel

import com.well.utils.Context
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.Closeable

expect class ContextHelper(context: Context) {
    val context: Context
    fun showAlert(alert: Alert)
    fun showSheet(vararg actions: Action): Closeable
    suspend fun pickSystemImage(): ImageContainer
}                           