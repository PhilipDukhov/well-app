package com.well.androidApp.ui.customViews

import com.well.androidApp.ui.theme.captionLight
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LimitedCharsTextField(
    valueState: MutableState<String>,
    labelText: String,
    maxCharacters: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
    ) {
        OutlinedTextField(
            value = valueState.value,
            onValueChange = {
                valueState.value = it
            },
            textStyle = MaterialTheme.typography.body1,
            label = { Text(labelText) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clearFocusOnKeyboardDismiss()
        )
        val count = valueState.value.count()
        Text(
            "$count/$maxCharacters",
            style = MaterialTheme.typography.captionLight,
            color = when {
                count == 0 -> Color.LightGray
                count <= maxCharacters -> Color.Green
                else -> Color.RadicalRed
            }.toColor(),
            modifier = Modifier
                .align(Alignment.End)
                .wrapContentHeight(unbounded = true)
        )
    }
}