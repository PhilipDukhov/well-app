package com.well.modules.androidUi.customViews

import androidx.compose.foundation.Image
import com.well.modules.androidUi.ext.thenOrNull
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ControlItem(
    val enabled: Boolean = true,
    val handler: () -> Unit = {},
    val view: @Composable BoxScope.() -> Unit
) {
    constructor(
        enabled: Boolean = true,
        text: String,
        handler: () -> Unit,
    ) : this(
        enabled,
        handler,
        { Text(text, color = Color.White) }
    )
}

@Composable
fun Control(item: ControlItem, modifier: Modifier = Modifier) {
    Control(
        enabled = item.enabled,
        onClick = item.handler,
        content = item.view,
        modifier = modifier,
    )
}

@Composable
fun Control(
    vectorResourceId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp? = null,
    onClick: () -> Unit,
) = Control(
    modifier = modifier,
    enabled = enabled,
    onClick = onClick,
) {
    Image(
        painterResource(id = vectorResourceId),
        contentDescription = null,
        modifier = Modifier
            .padding(5.dp)
            .thenOrNull(size?.let { Modifier.size(size) })
            .alpha(if (enabled) 1F else 0.4F)
    )
}

@Composable
fun Control(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = false),
            onClick = onClick,
        )
        .sizeIn(minHeight = controlMinSize, minWidth = controlMinSize)
        .alpha(if (enabled) 1f else 0.4f)
) {
    content()
}

val controlMinSize = 45.dp