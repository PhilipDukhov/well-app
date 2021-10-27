package com.well.modules.androidUi.composableScreens.myProfile.availability

import com.well.modules.androidUi.customViews.AutoSizeText
import com.well.modules.androidUi.customViews.GradientView
import com.well.modules.androidUi.customViews.swipeableLeftRight
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.thenOrNull
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.body1Light
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature.Msg
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature.State
import com.well.modules.models.Availability
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.models.date.dateTime.localizedVeryShortSymbol
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.datetime.LocalDate

private sealed class PresentingDialog {
    data class Create(val date: LocalDate) : PresentingDialog()
    data class Update(val availability: Availability) : PresentingDialog()
}

@Composable
fun CurrentUserAvailabilityView(
    state: State,
    listener: (Msg) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        var selectedDate by rememberSaveable(state.monthOffset) {
            mutableStateOf<LocalDate?>(null)
        }
        val selectedItem = remember(state.weeks, selectedDate) {
            state.weeks.firstNotNullOfOrNull { weekDays ->
                weekDays.firstOrNull { it.date == selectedDate }
            }
        }
        val (presentingDialog, setPresentingDialog) = rememberSaveable {
            mutableStateOf<PresentingDialog?>(null)
        }
        ProvideTextStyle(
            value = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
        ) {
            CalendarView(
                state = state,
                selectedDate = selectedDate,
                onSelectDate = {
                    selectedDate = if (selectedDate == it) null else it
                },
                showNextMonth = {
                    listener(Msg.NextMonth)
                },
                showPrevMonth = {
                    listener(Msg.PrevMonth)
                },
            )
        }
        AvailabilitiesList(
            selectedItem = selectedItem,
            allAvailabilities = state.monthAvailabilities,
            onSelect = {
                setPresentingDialog(PresentingDialog.Update(it))
            },
            onCreate = {
                setPresentingDialog(PresentingDialog.Create(it.date))
            },
        )
        if (presentingDialog != null) {
            val finish = remember {
                { msg: Msg? ->
                    setPresentingDialog(null)
                    msg?.let(listener)
                }
            }
            ProvideTextStyle(value = MaterialTheme.typography.body1Light) {
                when (presentingDialog) {
                    is PresentingDialog.Create -> {
                        CreateAvailability(
                            startDay = presentingDialog.date,
                            created = { availability ->
                                finish(Msg.Add(availability))
                            },
                            onCancel = {
                                finish(null)
                            },
                        )
                    }
                    is PresentingDialog.Update -> {
                        UpdateAvailability(
                            availability = presentingDialog.availability,
                            onSave = { availability ->
                                finish(Msg.Update(availability))
                            },
                            onCancel = {
                                finish(null)
                            },
                            onDelete = {
                                finish(Msg.Delete(presentingDialog.availability.id))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    state: State,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    showNextMonth: () -> Unit,
    showPrevMonth: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .swipeableLeftRight(
                onLeft = showNextMonth,
                onRight = showPrevMonth
            )
    ) {
        item(key = "month_title_${state.month}") {
            CalendarTitleView(state, onLeft = showPrevMonth, onRight = showNextMonth)
        }
        item(key = "week_days") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                State.allDaysOfWeek.forEach {
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
            state.weeks,
            key = { i, _ -> "week_${state.month}_${i}" }
        ) { _, week ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                week.forEach { day ->
                    DayView(day, selectedDate) {
                        onSelectDate(day.date)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AvailabilitiesList(
    selectedItem: State.CalendarItem?,
    allAvailabilities: List<Availability>,
    onSelect: (Availability) -> Unit,
    onCreate: (State.CalendarItem) -> Unit,
) {
    val padding = 10.dp
    CalculateAspectRatio(
        cellsCount = State.availabilityCellsCount,
        padding = padding,
    ) { aspectRatio ->
        LazyVerticalGrid(
            cells = GridCells.Fixed(State.availabilityCellsCount),
            verticalArrangement = Arrangement.spacedBy(padding),
            horizontalArrangement = Arrangement.spacedBy(padding),
            contentPadding = PaddingValues(vertical = padding),
            modifier = Modifier.navigationBarsPadding()
        ) {
            items(selectedItem?.availabilities ?: allAvailabilities) { availability ->
                AvailabilityCell(
                    firstRowText = selectedItem?.let { null }
                        ?: availability.startDay.localizedDayAndShortMonth,
                    secondRowText = availability.intervalString,
                    onClick = {
                        onSelect(availability)
                    },
                    aspectRatio = aspectRatio,
                )
            }
            selectedItem?.let { selectedItem ->
                if (selectedItem.canCreateAvailability) {
                    item {
                        AvailabilityCell(
                            onClick = {
                                onCreate(selectedItem)
                            },
                            aspectRatio = aspectRatio,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
    LazyRow {
        item {
            Modifier.fillParentMaxWidth()
        }
    }
}

@Composable
private fun AvailabilityCell(
    firstRowText: String?,
    secondRowText: String,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AvailabilityCell(
        onClick = onClick,
        aspectRatio = aspectRatio,
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            firstRowText?.let {
                AutoSizeText(it)
            }
            AutoSizeText(secondRowText)
        }
    }
}

@Composable
private fun AvailabilityCell(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    aspectRatio: Float?,
    content: @Composable BoxScope.() -> Unit,
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
        modifier = modifier
            .thenOrNull(aspectRatio?.let(Modifier::aspectRatio))
            .clip(shape)
            .clickable(onClick = onClick)
            .backgroundKMM(Color.LightBlue15, shape = shape)

    )
}

@Composable
private fun CalendarTitleView(
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
            state.calendarTitle,
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
    selectedDate: LocalDate?,
    onSelect: () -> Unit,
) {
    val selected = day.date == selectedDate
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
                            .alpha(if (selected) 1f else 0.5f)
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
                Spacer(Modifier.weight(1f))
                Text(
                    day.date.dayOfMonth.toString(),
                    color = textColor,
                )
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.weight(1f)
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

@Composable
private fun CalculateAspectRatio(
    @Suppress("SameParameterValue")
    cellsCount: Int,
    padding: Dp,
    content: @Composable (Float) -> Unit,
) {
    BoxWithConstraints {
        val (aspectRatio, setAspectRatio) = remember { mutableStateOf<Float?>(null) }
        if (aspectRatio == null) {
            AvailabilityCell(
                firstRowText = "",
                secondRowText = "",
                aspectRatio = null,
                modifier = Modifier
                    .drawWithContent { }
                    .width((maxWidth - padding * (cellsCount - 1)) / cellsCount)
                    .onSizeChanged {
                        setAspectRatio(it.width.toFloat() / it.height)
                    }
            )
        } else {
            content(aspectRatio)
        }
    }
}