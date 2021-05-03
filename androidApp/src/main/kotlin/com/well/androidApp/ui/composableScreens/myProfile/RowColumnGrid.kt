package com.well.androidApp.ui.composableScreens.myProfile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.well.modules.utils.forEachNamed

@Composable
fun RowColumnGrid(
    modifier: Modifier = Modifier,
    separator: Dp = 7.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        var currentRow = 0
        var currentOrigin = Origin.Zero
        val separatorValue = separator.toPx().toInt()
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentOrigin.x > 0f && currentOrigin.x + placeable.width > constraints.maxWidth) {
                currentRow += 1
                currentOrigin = currentOrigin.copyNextRow(y = placeable.height + separatorValue)
            }

            placeable to currentOrigin.also {
                currentOrigin = it.offset(x = placeable.width + separatorValue)
            }
        }

        layout(
            width = constraints.maxWidth,
            height = placeables.lastOrNull()?.run { first.height + second.y } ?: 0
        ) {
            placeables.forEachNamed { placeable, origin ->
                placeable.place(origin.x, origin.y)
            }
        }
    }
}

private data class Origin(
    val x: Int,
    val y: Int
) {
    fun offset(
        x: Int = 0,
        y: Int = 0,
    ) = copy(x = this.x + x, y = this.y + y)

    fun copyNextRow(y: Int) = copy(x = 0, y = this.y + y)

    companion object {
        val Zero = Origin(0, 0)
    }
}