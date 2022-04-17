package com.well.modules.androidUi.components

import com.well.modules.utils.viewUtils.GlobalStringsBase
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldDialog(
    send: (String) -> Unit,
    onDismissRequest: () -> Unit,
    sendButtonText: String,
    titleText: String,
    labelText: String,
) {
    DynamicContentSizeDialog(
        onDismissRequest = onDismissRequest,
    ) {
        TextFieldDialogContent(
            send = send,
            onDismissRequest = onDismissRequest,
            sendButtonText = sendButtonText,
            titleText = titleText,
            labelText = labelText,
        )
    }
}

@Composable
private fun TextFieldDialogContent(
    send: (String) -> Unit,
    onDismissRequest: () -> Unit,
    sendButtonText: String,
    titleText: String,
    labelText: String,
) {
    var text by rememberSaveable { mutableStateOf("") }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.surface,
        elevation = 24.dp,
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(maxWidth = 560.dp)
            .padding(16.dp)
    ) {
        val padding = 15.dp
        Column {
            AutoSizeText(
                titleText,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(padding)
            )
            val focusRequester = remember { FocusRequester() }

            // Replace with LaunchedEffect when issue solved
            // https://issuetracker.google.com/issues/204502668
            val view = LocalView.current
            DisposableEffect(Unit) {
                val listener = { focused: Boolean ->
                    if (focused) {
                        focusRequester.requestFocus()
                    }
                }
                view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
                onDispose {
                    view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
                }
            }
            LimitedCharsTextField(
                value = text,
                onValueChange = { text = it },
                labelText = labelText,
                maxCharacters = 150,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(horizontal = padding)
                    .height(IntrinsicSize.Min)
                    .weight(1f, fill = false)
            )
            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                ) {
                    Text(GlobalStringsBase.shared.cancel)
                }
                TextButton(
                    onClick = {
                        send(text)
                        onDismissRequest()
                    },
                    enabled = text.isNotBlank(),
                ) {
                    Text(sendButtonText)
                }
            }
        }
    }
}