package com.well.modules.androidUi.composableScreens.updateRequest

import com.well.modules.androidUi.R
import com.well.modules.androidUi.components.gradientBackground
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.updateRequest.UpdateRequestFeature.Msg
import com.well.modules.features.updateRequest.UpdateRequestFeature.State
import com.well.modules.models.Color
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun UpdateRequestScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    Box {
        Image(
            painter = painterResource(R.drawable.ic_overlay_message_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .gradientBackground(Gradient.Main)
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Image(
                painter = painterResource(R.drawable.ic_failed),
                contentDescription = null,
            )
            Text(
                state.text,
                color = Color.White.toColor(),
                style = MaterialTheme.typography.h4,
            )
        }
    }
}