package com.well.modules.androidUi.components

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    var scaledTextStyle by remember(text) { mutableStateOf(style) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    SubcomposeLayout { constraints ->
        var placeable: Placeable
        do {
            placeable = subcompose("${scaledTextStyle.fontSize}") {
                Text(
                    text = text,
                    style = scaledTextStyle,
                    softWrap = false,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.didOverflowWidth) {
                            scaledTextStyle = scaledTextStyle.copy(
                                fontSize = scaledTextStyle.fontSize * 0.9
                            )
                        } else {
                            readyToDraw = true
                        }
                    },
                    modifier = modifier
                        .drawWithContent {
                            if (readyToDraw) {
                                drawContent()
                            }
                        }
                )
            }[0].measure(constraints)
        } while (!readyToDraw)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}