package com.well.modules.androidUi.composableScreens.myProfile

import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.plus
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.body1Light
import com.well.modules.features.myProfile.myProfileFeature.SettingsFeature
import com.well.modules.features.myProfile.myProfileFeature.SettingsFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.SettingsFeature.State
import com.well.modules.models.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    ProvideTextStyle(
        MaterialTheme.typography.body1Light.copy(color = Color.Black.toColor())
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .backgroundKMM(Color.LightGray)
        ) {
            val contentPadding = ButtonDefaults.TextButtonContentPadding + PaddingValues(start = 10.dp)
            Column(
                Modifier
                    .backgroundKMM(Color.White)
            ) {
                TextButton(
                    onClick = {
                        listener(Msg.OpenTechnicalSupport)
                    },
                    contentPadding = contentPadding,
                ) {
                    Text(SettingsFeature.Strings.technicalSupport)
                    Spacer(Modifier.fillMaxWidth())
                }
                Divider()
                TextButton(
                    onClick = {
                        listener(Msg.Logout)
                    },
                    contentPadding = contentPadding,
                ) {
                    Text(SettingsFeature.Strings.logout)
                    Spacer(Modifier.fillMaxWidth())
                }
                Divider()
                TextButton(
                    onClick = {
                        listener(Msg.DeleteProfile)
                    },
                    contentPadding = contentPadding,
                ) {
                    Text(SettingsFeature.Strings.deleteProfile)
                    Spacer(Modifier.fillMaxWidth())
                }
                Divider()
            }
        }
    }
}