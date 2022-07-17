package com.well.modules.androidUi.components

import com.well.modules.androidUi.libs.accompanistInsets.ProvideWindowInsets
import com.well.modules.androidUi.libs.accompanistInsets.imePadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun DynamicContentSizeDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    // replace with foundation.imePadding after fix
    // https://issuetracker.google.com/issues/229378542
    @Suppress("DEPRECATION")
    ProvideWindowInsets {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }
}