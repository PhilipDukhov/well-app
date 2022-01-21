package com.well.modules.utils.viewUtils

import android.app.Activity
import android.content.Context
import androidx.annotation.DrawableRes

actual data class ApplicationContext(
    val context: Context,
    @DrawableRes val notificationResId: Int,
    val activityClass: Class<*>,
)