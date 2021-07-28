package com.well.androidApp.ui.composableScreens.πCustomViews

import com.well.sharedMobile.puerh.πModels.NavigationBarModel
import com.well.sharedMobile.utils.Gradient
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.statusBarsPadding

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    title: ControlItem? = null,
    leftItem: ControlItem? = null,
    rightItem: ControlItem? = null,
    contentHeight: Dp = controlMinSize,
) {
    Box(modifier = modifier) {
        GradientView(
            gradient = Gradient.Main,
            modifier = Modifier.matchParentSize(),
        )
        ConstraintLayout(
            modifier = Modifier
                .statusBarsPadding()
                .height(contentHeight)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            val (titleRef, leftItemRef, rightItemRef) = createRefs()
            ProvideTextStyle(
                value = MaterialTheme.typography.subtitle1
            ) {
                title?.let { title ->
                    Control(
                        title,
                        modifier = Modifier
                            .constrainAs(titleRef) {
                                centerTo(parent)
                            }
                    )
                }
            }
            ProvideTextStyle(
                value = MaterialTheme.typography.body1
            ) {
                leftItem?.let {
                    Control(
                        it,
                        modifier = Modifier
                            .constrainAs(leftItemRef) {
                                centerVerticallyTo(parent)
                                start.linkTo(parent.start)
                            }
                    )
                }
                rightItem?.let {
                    Control(
                        it,
                        modifier = Modifier
                            .constrainAs(rightItemRef) {
                                centerVerticallyTo(parent)
                                end.linkTo(parent.end)
                            }
                    )
                }
            }
        }
//        BoxWithConstraints(
//            modifier = Modifier
//                .padding(10.dp)
//                .statusBarsPadding()
//                .fillMaxWidth()
//                .height(controlMinSize)
//        ) {
//            title?.let { title ->
//                Control(
//                    title,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//            ProvideTextStyle(
//                value = MaterialTheme.typography.body1
//            ) {
//                leftItem?.let {
//                    Control(it, modifier = Modifier.align(Alignment.CenterStart))
//                }
//                rightItem?.let {
//                    Control(it, modifier = Modifier.align(Alignment.CenterEnd))
//                }
//            }
//        }
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
        title = model.title.toTitleControlItem(),
        leftItem = model.leftItem?.toControlItem(listener),
        rightItem = model.rightItem?.toControlItem(listener, rightItemBeforeAction),
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

@SuppressLint("ComposableNaming")
@Composable
fun String.toNavigationTitleText() =
    Text(
        this,
        color = Color.White,
    )

private fun String.toTitleControlItem() = ControlItem(view = { toNavigationTitleText() })

fun <Msg> NavigationBarModel.Item<Msg>.toControlItem(
    listener: (Msg) -> Unit,
    beforeAction: (() -> Unit)? = null
) =
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
