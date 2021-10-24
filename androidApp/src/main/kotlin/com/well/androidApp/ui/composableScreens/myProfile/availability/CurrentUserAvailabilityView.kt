package com.well.androidApp.ui.composableScreens.myProfile.availability

import com.well.androidApp.ui.customViews.AutoSizeText
import com.well.androidApp.ui.customViews.GradientView
import com.well.androidApp.ui.customViews.clickable
import com.well.androidApp.ui.customViews.swipeableLeftRight
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.toColor
import com.well.androidApp.ui.theme.body1Light
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature.Msg
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature.State
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature.Strings
import com.well.modules.models.Availability
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.models.date.dateTime.localizedName
import com.well.modules.models.date.dateTime.localizedVeryShortSymbol
import com.well.modules.viewHelpers.Gradient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.navigationBarsPadding
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.datetime.LocalDate

private sealed class PresentingDialog {
    data class Create(val date: LocalDate) : PresentingDialog()
    data class Update(val availability: Availability) : PresentingDialog()
}

@Composable
fun CurrentUserAvailabilityView(
    state: State,
    listener: (Msg) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 15.dp)
    ) {
        var selectedDate by rememberSaveable(state.monthOffset) {
            mutableStateOf<LocalDate?>(null)
        }
        val selectedItem = remember(state.days, selectedDate) {
            state.days.firstNotNullOfOrNull { weekDays ->
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
                onSwipeLeft = {
                    listener(Msg.NextMonth)
                },
                onSwipeRight = {
                    listener(Msg.PrevMonth)
                },
            )
        }
        Availabilities(
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
            MaterialDialog(
                dialogState = rememberMaterialDialogState(initialValue = true),
                onCloseRequest = {
                    setPresentingDialog(null)
                },
                buttons = {
                    positiveButton(Strings.add)
                    negativeButton(Strings.cancel, onClick = {
                        setPresentingDialog(null)
                    })
                    if (presentingDialog is PresentingDialog.Update) {
                        negativeButton(
                            Strings.delete,
                            textStyle = MaterialTheme.typography.button.copy(color = Color.RadicalRed.toColor()),
                            onClick = {
                            setPresentingDialog(null)
                            listener(Msg.Delete(presentingDialog.availability.id))
                        })
                    }
                }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.body1Light) {
                        when (presentingDialog) {
                            is PresentingDialog.Create -> {
                                Text(
                                    Strings.newAvailability,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                CreateAvailability(presentingDialog.date) { availability ->
                                    setPresentingDialog(null)
                                    listener(Msg.Add(availability))
                                }
                            }
                            is PresentingDialog.Update -> {
                                Text(
                                    Strings.updateAvailability,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                UpdateAvailability(presentingDialog.availability) { availability ->
                                    setPresentingDialog(null)
                                    listener(Msg.Update(availability))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Availabilities(
    selectedItem: State.CalendarItem?,
    allAvailabilities: List<Availability>,
    onSelect: (Availability) -> Unit,
    onCreate: (State.CalendarItem) -> Unit,
) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        items(selectedItem?.availabilities ?: allAvailabilities) { availability ->
            AvailabilityCell(
                onClick = {
                    onSelect(availability)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedItem == null) {
                        Text(availability.startDay.localizedDayAndShortMonth)
                    }
                    AutoSizeText(availability.intervalString)
                }
            }
        }
        selectedItem?.let { selectedItem ->
            if (selectedItem.canCreateAvailability) {
                item {
                    AvailabilityCell(
                        onClick = {
                            onCreate(selectedItem)
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
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
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
            TitleView(state, onLeft = onSwipeRight, onRight = onSwipeLeft)
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
                    DayView(day, selectedDate) {
                        onSelectDate(day.date)
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
    selectedDate: LocalDate?,
    onSelect: () -> Unit
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
                            .alpha(if (selectedDate == day.date) 1f else 0.5f)
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