package com.well.modules.androidUi.components

import com.well.modules.androidUi.ext.thenOrNull
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@Composable
fun rememberControlItem(
    vararg keys: Any?,
    handler: (() -> Unit)? = null,
    view: @Composable BoxScope.() -> Unit,
) = remember(*keys) {
    ControlItem(handler = handler, view = view)
}

@Composable
fun rememberControlItem(
    vararg keys: Any?,
    text: String,
    handler: (() -> Unit)? = null,
) = remember(*keys) {
    ControlItem(handler = handler, text = text)
}

data class ControlItem(
    val enabled: Boolean = true,
    val handler: (() -> Unit)? = null,
    val view: @Composable BoxScope.() -> Unit,
) {
    constructor(
        enabled: Boolean = true,
        text: String,
        handler: (() -> Unit)?,
    ) : this(
        enabled,
        handler,
        { Text(text, color = Color.White) }
    )

    constructor(
        text: String,
    ) : this(
        false,
        null,
        { Text(text, color = Color.White) }
    )

    companion object {
        fun back(handler: () -> Unit) =
            ControlItem(
                handler = handler,
                view = {
                    Icon(Icons.Default.ArrowBack, null)
                }
            )
    }
}

@Composable
fun Control(item: ControlItem, modifier: Modifier = Modifier) {
    val handler = item.handler
    if (handler != null) {
        Control(
            enabled = item.enabled,
            onClick = handler,
            content = item.view,
            modifier = modifier,
        )
    } else {
        Box(content = item.view)
    }
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
        .sizeIn(minHeight = controlMinSize, minWidth = controlMinSize)
        .alpha(if (enabled) 1f else com.well.modules.models.Color.inactiveAlpha)
        .clickable(
            enabled = enabled,
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(bounded = false),
            onClick = onClick,
        )
) {
    content()
}

val controlMinSize = 45.dp