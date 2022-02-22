package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedVeryShortSymbol
import com.well.modules.utils.viewUtils.CalendarInfoFeature
import com.well.modules.utils.viewUtils.CalendarMonthViewColors
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate

@Composable
fun <Event> CalendarMonthView(
    state: CalendarInfoFeature.State<Event>,
    title: @Composable CalendarTitleScope.() -> Unit,
    colors: CalendarMonthViewColors,
    onSelect: (LocalDate) -> Unit,
    showNextMonth: () -> Unit,
    showPrevMonth: () -> Unit,
) {
    ProvideTextStyle(
        value = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
    ) {
        LazyColumn(
            modifier = Modifier
                .swipeableLeftRight(
                    onLeft = showNextMonth,
                    onRight = showPrevMonth,
                )
        ) {
            item(key = "month_title_${state.month}") {
                remember(showNextMonth, showPrevMonth) {
                    TitleScopeImpl(
                        onShowNextMonth = showNextMonth,
                        onShowPrevMonth = showPrevMonth
                    )
                }.title()
            }
            item(key = "week_days") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CalendarInfoFeature.State.allDaysOfWeek.forEach {
                        Text(
                            it.localizedVeryShortSymbol,
                            color = colors.dayOfWeekColor.toColor(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            itemsIndexed(
                state.days,
                key = { i, _ -> "week_${state.month}_${i}" }
            ) { _, week ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    week.forEach { day ->
                        DayView(
                            day = day,
                            selectedDate = state.selectedDate,
                            colors = colors.dayColors
                        ) {
                            onSelect(day.date)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <Event> RowScope.DayView(
    day: CalendarInfoFeature.State.Item<Event>,
    selectedDate: LocalDate?,
    colors: CalendarMonthViewColors.DayColors,
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
                day.isCurrentDay && colors.currentDayBackgroundIsGradient -> {
                    Color.White
                }
                selected -> {
                    colors.selectedContentColor
                }
                else -> {
                    colors.nonSelectedContentColor
                }
            }.toColor().copy(alpha = if (day.isCurrentMonth) 1f else 0.5f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(35.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSelect
                )
                .drawBehind {
                    clipPath(Path().apply {
                        addOval(
                            Rect(
                                Offset.Zero,
                                size
                            )
                        )
                    }) {
                        when {
                            day.isCurrentDay && colors.currentDayBackgroundIsGradient -> {
                                drawGradient(
                                    Gradient.Main,
                                    alpha = if (selected) 1f else 0.5f
                                )
                            }
                            day.isCurrentDay || selected -> {
                                drawRect(
                                    color = colors.selectedBackgroundColor.toColor(),
                                    alpha = if (day.isCurrentDay && !selected) 0.3f else 1f
                                )
                            }
                        }
                    }
                }
        ) {
            Spacer(Modifier.weight(1f))
            BadgedBox(badge = {
                if (day.hasBadge) {
                    Badge()
                }
            }) {
                Text(
                    day.date.dayOfMonth.toString(),
                    color = textColor,
                )
            }
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.weight(1f)
            ) {
                if (day.events.isNotEmpty()) {
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

interface CalendarTitleScope {
    fun showNextMonth()
    fun showPrevMonth()
}

private class TitleScopeImpl(
    private val onShowNextMonth: () -> Unit,
    private val onShowPrevMonth: () -> Unit,
) : CalendarTitleScope {
    override fun showNextMonth() {
        onShowNextMonth()
    }

    override fun showPrevMonth() {
        onShowPrevMonth()
    }
}