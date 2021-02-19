package com.well.utils.ui

import android.content.Context
import android.util.TypedValue

fun Int.toDp(context: Context) = this.toFloat().toDp(context).toInt()

fun Float.toDp(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    context.resources.displayMetrics
)