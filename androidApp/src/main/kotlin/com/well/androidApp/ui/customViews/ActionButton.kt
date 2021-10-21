package com.well.androidApp.ui.customViews

import com.well.androidApp.ui.customViews.ActionButtonStyle.OnWhite
import com.well.androidApp.ui.customViews.ActionButtonStyle.White
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.thenOrNull
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.utils.Gradient
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
fun ActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ActionButtonStyle = OnWhite,
    enabled: Boolean = true,
    label: @Composable () -> Unit,
) {
    val alpha = if (enabled) 1f else 0.4f
    ProvideTextStyle(
        value = MaterialTheme.typography.subtitle1
    ) {
        CompositionLocalProvider(
            LocalContentColor provides when (style) {
                White -> Color.MediumBlue
                OnWhite -> Color.White
            }.toColor()
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
                        gradient = Gradient.Main,
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