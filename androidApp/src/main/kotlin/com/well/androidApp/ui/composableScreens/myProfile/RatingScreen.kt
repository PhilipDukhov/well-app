package com.well.androidApp.ui.composableScreens.myProfile

import com.well.androidApp.ui.customViews.ActionButton
import com.well.androidApp.ui.customViews.Control
import com.well.androidApp.ui.customViews.LimitedCharsTextField
import com.well.androidApp.ui.customViews.NavigationBar
import com.well.androidApp.ui.customViews.ProfileImage
import com.well.androidApp.ui.ext.toColor
import com.well.androidApp.ui.ext.widthDp
import com.well.modules.models.Color
import com.well.modules.models.Rating
import com.well.modules.models.User
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RatingScreen(
    user: User,
    maxCharacters: Int,
    rate: (Rating) -> Unit,
) {
    var ratingValue by remember { mutableStateOf(user.ratingInfo.currentUserRating?.value ?: 0) }
    val ratingTextFieldValueState = remember {
        mutableStateOf(user.ratingInfo.currentUserRating?.text ?: "")
    }
    val profileImageSize = LocalContext.current.resources.displayMetrics.widthDp * 0.42f
    val imageTopPadding = 20.dp
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigationBar(
            contentHeight = profileImageSize / 2 + imageTopPadding
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = imageTopPadding)
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = !LocalWindowInsets.current.ime.isVisible,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfileImage(
                        user = user,
                        modifier = Modifier
                            .size(profileImageSize)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        if (user.ratingInfo.currentUserRating != null)
                            "Update your review"
                        else
                            "Please write a review about",
                        style = MaterialTheme.typography.body2,
                    )
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.h4,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                (1..5).forEach { star ->
                    val selected = star <= ratingValue
                    Control(onClick = {
                        ratingValue = star
                    }) {
                        Icon(
                            imageVector = if (selected) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "",
                            tint = (if (selected) Color.Green else Color.LightGray).toColor()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
            LimitedCharsTextField(
                valueState = ratingTextFieldValueState,
                maxCharacters = maxCharacters,
                labelText = "Tell us about your experience",
                modifier = Modifier
                    .padding(10.dp)
                    .heightIn(min = 150.dp)
                    .weight(1f)
            )
            val text = ratingTextFieldValueState.value
            ActionButton(
                enabled = text.isNotBlank(),
                onClick = {
                    rate(Rating(ratingValue, text))
                },
                modifier = Modifier
                    .navigationBarsWithImePadding()
                    .padding(10.dp)
            ) {
                Text("Send")
            }
        }
    }
}