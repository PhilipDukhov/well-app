package com.well.modules.androidUi.components

import com.well.modules.utils.viewUtils.Gradient
import com.well.modules.utils.viewUtils.NavigationBarModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    title: ControlItem? = null,
    leftItem: ControlItem? = null,
    rightItem: ControlItem? = null,
    contentHeight: Dp = controlMinSize,
    extraContent: @Composable () -> Unit = {},
) {
    Box(modifier = modifier) {
        GradientView(
            gradient = Gradient.NavBar,
            modifier = Modifier.matchParentSize(),
        )
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column {
                ConstraintLayout(
                    modifier = Modifier
                        .statusBarsPadding()
                        .heightIn(min = contentHeight)
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    val (titleRef, leftItemRef, rightItemRef) = createRefs()
                    ProvideTextStyle(
                        value = MaterialTheme.typography.subtitle1
                    ) {
                        AnimatedOptionalContent(
                            targetState = title,
                            modifier = Modifier
                                .constrainAs(titleRef) {
                                    centerTo(parent)
                                }
                        ) {
                            Control(it)
                        }
                    }
                    ProvideTextStyle(
                        value = MaterialTheme.typography.body1
                    ) {
                        AnimatedOptionalContent(
                            targetState = leftItem,
                            modifier = Modifier
                                .constrainAs(leftItemRef) {
                                    centerVerticallyTo(parent)
                                    start.linkTo(parent.start)
                                }
                        ) {
                            Control(it)
                        }
                        AnimatedOptionalContent(
                            targetState = rightItem,
                            modifier = Modifier
                                .constrainAs(rightItemRef) {
                                    centerVerticallyTo(parent)
                                    end.linkTo(parent.end)
                                }
                        ) {
                            Control(it)
                        }
                    }
                }
                extraContent()
            }
        }
    }
}

@Composable
fun <Msg> ModeledNavigationBar(
    model: NavigationBarModel<Msg>,
    listener: (Msg) -> Unit,
    modifier: Modifier = Modifier,
    rightItemBeforeAction: (() -> Unit)? = null,
    extraContent: @Composable () -> Unit = {},
) {
    NavigationBar(
        title = model.title.toTitleControlItem(),
        leftItem = model.leftItem?.toControlItem(listener),
        rightItem = model.rightItem?.toControlItem(listener, rightItemBeforeAction),
        extraContent = extraContent,
        modifier = modifier,
    )
}


@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    title: String,
    leftItem: ControlItem? = null,
    rightItem: ControlItem? = null,
) {
    NavigationBar(
        modifier = modifier,
        title = title.toTitleControlItem(),
        leftItem = leftItem,
        rightItem = rightItem,
    )
}

@Composable
private fun String.toTitleControlItem() =
    ControlItem(view = remember(this) { { Text(this@toTitleControlItem) } })

@Composable
fun <Msg> NavigationBarModel.Item<Msg>.toControlItem(
    listener: (Msg) -> Unit,
    beforeAction: (() -> Unit)? = null,
) =
    ControlItem(
        enabled = enabled,
        handler = remember(msg, beforeAction) {
            { beforeAction?.invoke(); msg?.let { listener(it) } }
        },
        view = remember(content) { { ItemContentView(content) } },
    )

@Composable
fun ItemContentView(content: NavigationBarModel.Item.Content) {
    when (content) {
        is NavigationBarModel.Item.Content.Text -> {
            Text(content.text)
        }
        NavigationBarModel.Item.Content.ActivityIndicator -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(controlMinSize * 0.7f)
            )
        }
        is NavigationBarModel.Item.Content.Icon -> {
            when (content.icon) {
                NavigationBarModel.Item.Content.Icon.Icon.Back -> {
                    Icon(Icons.Default.ArrowBack, null)
                }
            }
        }
    }
}