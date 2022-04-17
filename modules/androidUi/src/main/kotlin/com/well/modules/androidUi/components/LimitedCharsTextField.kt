package com.well.modules.androidUi.components

import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.captionLight
import com.well.modules.models.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.text.trimmedLength

@Composable
fun LimitedCharsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    maxCharacters: Int,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Column(
        modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.body1,
            label = { Text(labelText) },
            maxLines = maxLines,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clearFocusOnKeyboardDismiss()
        )
        val count = value.trimmedLength()
        Text(
            "$count/$maxCharacters",
            style = MaterialTheme.typography.captionLight,
            color = when {
                value.isBlank() -> Color.LightGray
                count <= maxCharacters -> Color.Green
                else -> Color.RadicalRed
            }.toColor(),
            modifier = Modifier
                .align(Alignment.End)
                .wrapContentHeight(unbounded = true)
        )
    }
}