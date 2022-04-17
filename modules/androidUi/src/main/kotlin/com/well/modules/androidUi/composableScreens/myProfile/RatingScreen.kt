package com.well.modules.androidUi.composableScreens.myProfile

import com.well.modules.androidUi.components.ActionButton
import com.well.modules.androidUi.components.Control
import com.well.modules.androidUi.components.LimitedCharsTextField
import com.well.modules.androidUi.components.NavigationBar
import com.well.modules.androidUi.components.ProfileImage
import com.well.modules.androidUi.ext.isImeVisible
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.widthDp
import com.well.modules.models.Color
import com.well.modules.models.Review
import com.well.modules.models.User
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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

@Composable
fun RatingScreen(
    user: User,
    maxCharacters: Int,
    rate: (Review) -> Unit,
) {
    var ratingValue by remember { mutableStateOf(user.reviewInfo.currentUserReview?.value ?: 0) }
    var ratingTextFieldValue by remember {
        mutableStateOf(user.reviewInfo.currentUserReview?.text ?: "")
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
                visible = !WindowInsets.isImeVisible,
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
                        if (user.reviewInfo.currentUserReview != null)
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
                            contentDescription = null,
                            tint = (if (selected) Color.Green else Color.LightGray).toColor()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
            LimitedCharsTextField(
                value = ratingTextFieldValue,
                onValueChange = { ratingTextFieldValue = it },
                maxCharacters = maxCharacters,
                labelText = "Tell us about your experience",
                modifier = Modifier
                    .padding(10.dp)
                    .heightIn(min = 150.dp)
                    .weight(1f),
            )
            ActionButton(
                enabled = ratingTextFieldValue.isNotBlank(),
                onClick = {
                    rate(Review(ratingValue, ratingTextFieldValue))
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(10.dp)
            ) {
                Text("Send")
            }
        }
    }
}