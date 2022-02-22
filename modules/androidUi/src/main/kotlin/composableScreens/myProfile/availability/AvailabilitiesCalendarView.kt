package com.well.modules.androidUi.composableScreens.myProfile.availability

import com.well.modules.androidUi.customViews.AutoSizeText
import com.well.modules.androidUi.customViews.CalendarMonthView
import com.well.modules.androidUi.customViews.InactiveOverlay
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.thenOrNull
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.body1Light
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature.State
import com.well.modules.models.Availability
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
import com.well.modules.utils.viewUtils.CalendarInfoFeature
import com.well.modules.utils.viewUtils.CalendarMonthViewColors
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature as Feature
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.math.floor
import kotlin.math.roundToInt

private sealed interface PresentingDialog {
    data class Create(val date: LocalDate) : PresentingDialog
    data class Update(val availability: Availability) : PresentingDialog
}

private typealias CalendarItem = CalendarInfoFeature.State.Item<Availability>

@Composable
fun AvailabilitiesCalendarView(
    state: State,
    listener: (Msg) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(horizontal = 15.dp)
        ) {
            val (presentingDialog, setPresentingDialog) = rememberSaveable {
                mutableStateOf<PresentingDialog?>(null)
            }
            CalendarMonthView(
                state = state.infoState,
                title = {
                    TitleView(state, onLeft = ::showPrevMonth, onRight = ::showNextMonth)
                },
                colors = CalendarMonthViewColors.availabilitiesCalendar,
                onSelect = {
                    listener(Msg.SelectDate(it))
                },
                showNextMonth = {
                    listener(Msg.NextMonth)
                },
                showPrevMonth = {
                    listener(Msg.PrevMonth)
                },
            )
            AvailabilitiesList(
                selectedItem = state.infoState.selectedItem,
                allAvailabilities = state.infoState.monthEvents,
                onSelect = {
                    setPresentingDialog(PresentingDialog.Update(it))
                },
                onCreate = {
                    setPresentingDialog(PresentingDialog.Create(it.date))
                },
                canCreateAvailability = state::canCreateAvailability,
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
        if (!state.loaded || state.processing) {
            val failureReason = state.failureReason
            InactiveOverlay(showActivityIndicator = failureReason == null) {
                if (failureReason != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(failureReason)
                        TextButton(
                            onClick = { listener(Msg.ReloadAvailabilities) },
                        ) {
                            Text(
                                Feature.Strings.tryAgain,
                                color = Color.MediumBlue.toColor(),
                                style = MaterialTheme.typography.subtitle1,
                            )
                        }
                    }
                } else if (!state.loaded) {
                    LaunchedEffect(Unit) {
                        listener(Msg.ReloadAvailabilities)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AvailabilitiesList(
    selectedItem: CalendarItem?,
    allAvailabilities: List<Availability>,
    onSelect: (Availability) -> Unit,
    onCreate: (CalendarItem) -> Unit,
    canCreateAvailability: (CalendarItem) -> Boolean,
) {
    val spacing = 10.dp
    val availabilities = selectedItem?.events ?: allAvailabilities
    CalculateSize(
        cellsCount = State.availabilityCellsCount,
        spacing = spacing,
        includeFirst = selectedItem == null,
    ) { size ->
        LazyVerticalGrid(
            cells = GridCells.Fixed(State.availabilityCellsCount),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(vertical = spacing),
        ) {
            items(availabilities) { availability ->
                AvailabilityCell(
                    firstRowText = ifTrueOrNull(selectedItem == null) {
                        availability.startDay.localizedDayAndShortMonth()
                    },
                    secondRowText = availability.intervalString,
                    onClick = {
                        onSelect(availability)
                    },
                    size = size,
                )
            }
            selectedItem?.let { selectedItem ->
                if (canCreateAvailability(selectedItem)) {
                    item {
                        AvailabilityCell(
                            onClick = {
                                onCreate(selectedItem)
                            },
                            size = size,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityCell(
    firstRowText: String?,
    secondRowText: String,
    size: DpSize?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AvailabilityCell(
        onClick = onClick,
        size = size,
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
    size: DpSize?,
    content: @Composable BoxScope.() -> Unit,
) {
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
            .thenOrNull(size?.let(Modifier::size))
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .backgroundKMM(Color.LightBlue15, shape = MaterialTheme.shapes.medium)
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
private fun CalculateSize(
    @Suppress("SameParameterValue")
    cellsCount: Int,
    spacing: Dp,
    includeFirst: Boolean,
    content: @Composable (DpSize) -> Unit,
) {
    val (size, setSize) = remember(includeFirst) { mutableStateOf<DpSize?>(null) }
    if (size == null) {
        BoxWithConstraints {
            val width = ((maxWidth - spacing * (cellsCount - 1)) / cellsCount).value.roundToInt().dp
            val density = LocalDensity.current
            AvailabilityCell(
                firstRowText = if (includeFirst) "" else null,
                secondRowText = "",
                size = null,
                modifier = Modifier
                    .drawWithContent { }
                    .width(width)
                    .onSizeChanged {
                        val minCellHeight = with(density) { it.height.toDp() }
                        val availableContainerHeight = maxHeight - spacing
                        val itemsCount =
                            floor((availableContainerHeight / (minCellHeight + spacing)) + 0.5f) - 0.5f
                        val height = (availableContainerHeight / itemsCount - spacing)
                        setSize(DpSize(width, height))
                    }
            )
        }
    } else {
        Box {
            content(size)
        }
    }
}