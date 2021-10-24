package com.well.modules.androidUi.customViews

import android.app.Dialog
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberAndroidDialog(vararg keys: Any?, factory: (Context) -> Dialog) : Dialog {
    val context = LocalContext.current
    var resetDialogFlag by remember { mutableStateOf(false) }
    val timePickerDialog = remember(*(keys.toList() + listOf(resetDialogFlag)).toTypedArray()) {
        factory(context)
    }
    if (timePickerDialog.isShowing) {
        DisposableEffect(Unit) {
            onDispose {
                resetDialogFlag = !resetDialogFlag
            }
        }
    }
    return timePickerDialog
}