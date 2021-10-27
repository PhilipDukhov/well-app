package com.well.modules.androidUi.composableScreens.myProfile.availability

import com.well.modules.androidUi.customViews.rememberAndroidDialog
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.featureListener
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.myProfile.currentUserAvailability.CreateAvailabilityFeature.Msg
import com.well.modules.features.myProfile.currentUserAvailability.CreateAvailabilityFeature.Strings
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import com.well.modules.models.Availability
import com.well.modules.models.Color
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.features.myProfile.currentUserAvailability.CreateAvailabilityFeature as Feature
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogButtons
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.listItemsSingleChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Composable
fun CreateAvailability(
    startDay: LocalDate,
    created: (Availability) -> Unit,
    onCancel: () -> Unit,
) {
    UpdateAvailability(
        initial = Feature.initialStateCreate(startDay),
        onSave = created,
        onCancel = onCancel
    )
}

@Composable
fun UpdateAvailability(
    availability: Availability,
    onSave: (Availability) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    UpdateAvailability(
        Feature.initialStateUpdate(availability),
        onSave = onSave,
        onDelete = onDelete,
        onCancel = onCancel
    )
}

@Composable
private fun UpdateAvailability(
    initial: Feature.State,
    onSave: (Availability) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit = {},
) {
    val mutableState = rememberSaveable(saver = StateSaver) {
        mutableStateOf(initial)
    }
    val state = mutableState.value
    val listener = featureListener(
        mutableState,
        Feature::reducer,
        effHandler = { eff ->
            when (eff) {
                is Feature.Eff.Save -> {
                    onSave(eff.availability)
                }
                is Feature.Eff.Delete -> {
                    onDelete()
                }
            }
        }
    )
    MaterialDialog(
        dialogState = rememberMaterialDialogState(initialValue = true),
        onCloseRequest = {
            onCancel()
        },
        buttons = {
            negativeButton(
                CurrentUserAvailabilitiesListFeature.Strings.cancel,
                onClick = onCancel
            )
            if (state.type == Feature.State.Type.Editing) {
                negativeButton(
                    CurrentUserAvailabilitiesListFeature.Strings.delete,
                    textStyle = MaterialTheme.typography.button.copy(color = Color.RadicalRed.toColor()),
                    onClick = onDelete
                )
            }
            positiveButton(
                state.finishButtonTitle,
                onClick = {
                    listener(Msg.Save)
                }
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            PositiveButtonEnabled(valid = state.valid) {}
            Text(state.title)

            Divider(Modifier.backgroundKMM(Color.LightGray))
            TimerPickerRow(Strings.start,
                state.availability.startTime,
                valid = state.startTimeValid) {
                listener(
                    Msg.SetStartTime(it)
                )
            }

            Divider(Modifier.backgroundKMM(Color.LightGray))
            TimerPickerRow(Strings.end, state.availability.endTime, valid = state.endTimeValid) {
                listener(
                    Msg.SetEndTime(it)
                )
            }

            Divider(Modifier.backgroundKMM(Color.LightGray))

            RepeatRow(state.availability.repeat) {
                listener(Msg.SetRepeat(it))
            }

            Divider(Modifier.backgroundKMM(Color.LightGray))
        }
    }
}

@Composable
private fun TimerPickerRow(
    title: String,
    initial: LocalTime,
    valid: Boolean = true,
    update: (LocalTime) -> Unit,
) {
    val timePickerDialog = rememberAndroidDialog(initial) { context ->
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                update(LocalTime(hour = hourOfDay, minute = minute))
            },
            initial.hour,
            initial.minute,
            true
        )
    }
    Cell(
        onClick = {
            timePickerDialog.show()
        }
    ) {
        Text(title)
        Text(
            remember(initial) { initial.toString() },
            style = LocalTextStyle.current.copy(
                textDecoration = if (valid) TextDecoration.None else TextDecoration.LineThrough
            )
        )
    }
}

@Composable
private fun RepeatRow(repeat: Repeat, onUpdate: (Repeat) -> Unit) {
    DialogRow(
        buttons = {
            negativeButton(Strings.cancel)
        },
        dialogContent = {
            listItemsSingleChoice(
                list = Repeat.values().map(Repeat::name),
                initialSelection = repeat.ordinal,
                waitForPositiveButton = false,
                onChoiceChange = {
                    submit()
                    onUpdate(Repeat.values()[it])
                }
            )
        }
    ) {
        Text(Strings.repeat)
        Text(repeat.name)
    }
}

@Composable
private fun DialogRow(
    buttons: @Composable MaterialDialogButtons.() -> Unit = {},
    dialogContent: @Composable MaterialDialogScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val dialogState = rememberMaterialDialogState(false)
    Box {
        MaterialDialog(
            dialogState = dialogState,
            buttons = buttons,
            content = dialogContent
        )
        Cell(
            onClick = {
                dialogState.show()
            },
            content = content,
        )
    }
}

@Composable
private fun Cell(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Box {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        )
    }
}

private val StateSaver = Saver<MutableState<Feature.State>, Any>(
    save = { mutableState ->
        mutableState.value.let { state ->
            state.availability.run {
                listOf(
                    id.toString(),
                    startInstant.toString(),
                    durationMinutes.toString(),
                    repeat.ordinal.toString(),
                    state.type.ordinal,
                )
            }
        }
    },
    restore = {
        @Suppress("UNCHECKED_CAST")
        val iterator = (it as List<String>).iterator()

        mutableStateOf(
            Feature.State(
                availability = Availability(
                    id = iterator.next().toInt(),
                    startInstant = Instant.parse(iterator.next()),
                    durationMinutes = iterator.next().toInt(),
                    repeat = Repeat.values()[iterator.next().toInt()]
                ),
                type = Feature.State.Type.values()[iterator.next().toInt()],
            )
        ).also {
            if (iterator.hasNext()) {
                error("AvailabilitySaver unexpected next: ${iterator.next()}")
            }
        }
    }
)