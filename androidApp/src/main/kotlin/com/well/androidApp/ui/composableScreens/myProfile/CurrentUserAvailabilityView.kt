package com.well.androidApp.ui.composableScreens.myProfile

import com.well.androidApp.ui.composableScreens.πCustomViews.AutoSizeText
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πCustomViews.GradientView
import com.well.androidApp.ui.composableScreens.πCustomViews.clickable
import com.well.androidApp.ui.composableScreens.πCustomViews.swipeableLeftRight
import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color
import com.well.modules.utils.date.hoursAndMinutes
import com.well.modules.utils.date.intervalString
import com.well.modules.utils.date.localizedDayAndShortMonth
import com.well.modules.utils.date.localizedName
import com.well.modules.utils.date.localizedShortName
import com.well.modules.utils.date.localizedVeryShortSymbol
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilityFeature
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilityFeature.Msg
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilityFeature.State
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.yearAndDay
import com.well.sharedMobile.testData.testState
import com.well.sharedMobile.utils.Gradient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.util.date.*

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

@Composable
fun CurrentUserAvailabilityView(
    state: State,
    listener: (Msg) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        var selectedItem by rememberSaveable(state.monthOffset) {
            mutableStateOf<State.CalendarItem?>(null)
        }
        ProvideTextStyle(
            value = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
        ) {
            CalendarView(
                state = state,
                selectedItem = selectedItem,
                onSelectItem = {
                    selectedItem = if (selectedItem == it) null else it
                },
                onSwipeLeft = {
                    listener(Msg.PrevMonth)
                },
                onSwipeRight = {
                    listener(Msg.NextMonth)
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            cells = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
        ) {
            items(selectedItem?.availabilities ?: state.allAvailabilities) { availability ->
                AvailabilityCell {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedItem == null) {
                            Text(availability.startTime.localizedDayAndShortMonth)
                        }
                        AutoSizeText(availability.intervalString)
                    }
                }
            }
            selectedItem?.let {
                item {
                    AvailabilityCell(
                        onClick = {

                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    state: State,
    selectedItem: State.CalendarItem?,
    onSelectItem: (State.CalendarItem) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .swipeableLeftRight(
                onLeft = onSwipeLeft,
                onRight = onSwipeRight
            )
    ) {
        item(key = "month_title_${state.month}") {
            TitleView(state, onLeft = onSwipeLeft, onRight = onSwipeRight)
        }
        item(key = "week_days") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.allDaysOfWeek.forEach {
                    Text(
                        it.localizedVeryShortSymbol,
                        color = Color.LightBlue.toColor(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        itemsIndexed(
            state.days,
            key = { i, _ -> "week_${state.month}_${i}" }
        ) { _, weekDays ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                weekDays.forEach { day ->
                    DayView(day, selectedItem?.date) {
                        onSelectItem(day)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityCell(
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        content = {
            ProvideTextStyle(
                value = MaterialTheme.typography.body2.copy(color = Color.DarkGrey.toColor())
            ) {
                Box(Modifier.padding(10.dp)) {
                    content()
                }
            }
        },
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1.89f)
            .clip(shape)
            .clickable(onClick = onClick)
            .backgroundKMM(Color.LightBlue15, shape = shape)

    )
}

@Composable
private fun TitleView(
    state: State,
    onLeft: () -> Unit,
    onRight: () -> Unit,
) {
    Row {
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onLeft) {
            Icon(Icons.Default.ArrowBack, contentDescription = "")
        }
        Text(
            state.month.localizedName + (state.year?.let { ", $it" } ?: ""),
            style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Light),
        )
        IconButton(onClick = onRight) {
            Icon(Icons.Default.ArrowForward, contentDescription = "")
        }
        Spacer(Modifier.weight(1f))
    }
}


@Composable
private fun RowScope.DayView(
    day: State.CalendarItem,
    selectedDate: GMTDate?,
    onSelect: () -> Unit
) {
    val selected = day.date.yearAndDay == selectedDate?.yearAndDay
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)

    ) {
        val textColor = remember(day.isCurrentDay, selected, day.isCurrentMonth) {
            when {
                day.isCurrentDay -> {
                    Color.White
                }
                selected -> {
                    Color.White
                }
                else -> {
                    Color.DarkGrey
                }
            }.toColor().copy(alpha = if (day.isCurrentMonth) 1f else 0.5f)
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(35.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSelect
                )
        ) {
            when {
                day.isCurrentDay -> {
                    GradientView(
                        gradient = Gradient.Main,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(if (selectedDate?.yearAndDay == day.date.yearAndDay) 1f else 0.5f)
                    )
                }
                selected -> {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Color.DarkGrey
                                    .toColor()
                                    .copy(alpha = if (day.isCurrentMonth) 1f else 0.5f)
                            )
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f, fill = false))
                Text(
                    day.date.dayOfMonth.toString(),
                    color = textColor,
                )
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (day.availabilities.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .background(textColor, shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

val map = mutableMapOf<Int, IntSize>()