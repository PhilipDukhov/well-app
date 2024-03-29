package com.well.modules.androidUi.components

import com.well.modules.androidUi.components.ActionButtonStyle.OnWhite
import com.well.modules.androidUi.components.ActionButtonStyle.White
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.thenOrNull
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

enum class ActionButtonStyle {
    White,
    OnWhite,
    ;
}

@Composable
fun <T> ActionButton(
    state: T?,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    style: ActionButtonStyle = OnWhite,
    label: @Composable () -> Unit,
) {
    ActionButton(
        onClick = {
            if (state != null) {
                onClick(state)
            }
        },
        enabled = state != null,
        style = style,
        label = label,
        modifier = modifier,
    )
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ActionButtonStyle = OnWhite,
    enabled: Boolean = true,
    label: @Composable () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.4f
    val contentColor = when (style) {
        White -> Color.MediumBlue
        OnWhite -> Color.White
    }.toColor()
    ProvideTextStyle(
        value = MaterialTheme.typography.subtitle1.copy(color = contentColor)
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            Control(
                onClick = onClick,
                modifier = modifier
                    .requiredHeight(57.dp)
                    .fillMaxWidth()
                    .thenOrNull(
                        if (style == White)
                            Modifier.backgroundKMM(Color.White.copy(alpha = alpha))
                        else null,
                    )
                    .clip(RoundedCornerShape(100))
            ) {
                if (style == OnWhite) {
                    GradientView(
                        gradient = Gradient.ActionButton,
                        modifier = Modifier
                            .alpha(alpha)
                            .matchParentSize()
                    )
                }
                label()
            }
        }
    }
}