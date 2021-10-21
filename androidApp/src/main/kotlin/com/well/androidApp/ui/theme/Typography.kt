package com.well.androidApp.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = androidx.compose.material.Typography(
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 21.sp,
        letterSpacing = 0.15.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.1.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.25.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp
    ),
)

val androidx.compose.material.Typography.captionLight: TextStyle
    get() = caption.copy(fontWeight = FontWeight.Light)

val androidx.compose.material.Typography.body1Light: TextStyle
    get() = body1.copy(fontWeight = FontWeight.Light)

val androidx.compose.material.Typography.body2Light: TextStyle
    get() = body2.copy(fontWeight = FontWeight.Light)