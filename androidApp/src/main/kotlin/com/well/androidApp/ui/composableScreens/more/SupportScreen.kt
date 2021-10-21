package com.well.androidApp.ui.composableScreens.more

import com.well.androidApp.ui.customViews.ActionButton
import com.well.androidApp.ui.customViews.LimitedCharsTextField
import com.well.androidApp.ui.customViews.NavigationBar
import com.well.sharedMobile.puerh.more.support.SupportFeature.Msg
import com.well.sharedMobile.puerh.more.support.SupportFeature.State
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding

@Composable
fun ColumnScope.SupportScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(title = "Support")
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(10.dp)
            .navigationBarsWithImePadding()
    ) {
        val textFieldValueState = remember { mutableStateOf("") }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                state.text,
                style = MaterialTheme.typography.body1
            )
            LimitedCharsTextField(
                valueState = textFieldValueState,
                maxCharacters = state.maxCharacters,
                labelText = "Please write your message",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
        ActionButton(
            enabled = textFieldValueState.value.isNotBlank(),
            onClick = {
                listener(Msg.Send(textFieldValueState.value))
            }
        ) {
            Text("Send")
        }
    }
}