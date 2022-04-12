package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.customViews.ActionButton
import com.well.modules.androidUi.customViews.LimitedCharsTextField
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.androidUi.customViews.SwitchRow
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.features.more.moreFeature.subfeatures.SupportFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.SupportFeature.State
import com.well.modules.models.Color
import com.well.modules.utils.viewUtils.GlobalStringsBase
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.SupportScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    Box {
        Column {
            NavigationBar(title = state.title)
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(10.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                var text by remember { mutableStateOf("") }
                var includeLogs by remember { mutableStateOf(true) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        state.text,
                        style = MaterialTheme.typography.body1
                    )
                    LimitedCharsTextField(
                        value = text,
                        onValueChange = { text = it },
                        maxCharacters = state.maxCharacters,
                        labelText = "Please write your message",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                    )
                }
                SwitchRow(
                    text = state.includeLogs,
                    checked = includeLogs,
                    onCheckedChange = { includeLogs = it },
                )
                ActionButton(
                    enabled = text.isNotBlank(),
                    onClick = {
                        listener(Msg.Send(text, includeLogs = includeLogs))
                    },
                ) {
                    Text(GlobalStringsBase.shared.send)
                }
            }
        }
        if (state.processing) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .matchParentSize()
                    .backgroundKMM(Color.InactiveOverlay)
            ) {
                CircularProgressIndicator(Modifier.fillMaxSize(0.2f))
            }
        }
    }
}