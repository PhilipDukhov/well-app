package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.well.sharedMobile.puerh.πModels.NavigationBarModel
import com.well.sharedMobile.utils.Gradient
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun NavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    leftItem: ControlItem? = null,
    rightItem: ControlItem? = null,
) {
    Box(modifier = modifier) {
        GradientView(
            gradient = Gradient.Main,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .padding(10.dp)
                .statusBarsPadding()
                .fillMaxWidth()
                .height(controlMinSize)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )
            ProvideTextStyle(
                value = MaterialTheme.typography.body1
            ) {
                leftItem?.let {
                    Control(it, modifier = Modifier.align(Alignment.CenterStart))
                }
                rightItem?.let {
                    Control(it, modifier = Modifier.align(Alignment.CenterEnd))
                }
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
) {
    NavigationBar(
        title = model.title,
        leftItem = model.leftItem?.toControlItem(listener),
        rightItem = model.rightItem?.toControlItem(listener, rightItemBeforeAction),
        modifier = modifier,
    )
}

fun <Msg> NavigationBarModel.Item<Msg>.toControlItem(listener: (Msg) -> Unit, beforeAction: (() -> Unit)? = null) =
    if ((content as? NavigationBarModel.Item.Content.Icon)?.icon != NavigationBarModel.Item.Content.Icon.Icon.Back) {
        ControlItem(
            enabled,
            { beforeAction?.invoke(); msg?.let { listener(it) } },
            { ItemContentView(content) },
        )
    } else null

@Composable
fun ItemContentView(content: NavigationBarModel.Item.Content) {
    when (content) {
        is NavigationBarModel.Item.Content.Text -> {
            Text(content.text, color = Color.White)
        }
        NavigationBarModel.Item.Content.ActivityIndicator -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(controlMinSize * 0.7f)
            )
        }
        is NavigationBarModel.Item.Content.Icon -> {
            when (content.icon) {
                NavigationBarModel.Item.Content.Icon.Icon.Back ->
                    throw IllegalStateException()
            }
        }
    }
}
