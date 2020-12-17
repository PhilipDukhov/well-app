package com.well.androidApp.ui.composableScreens.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = androidx.compose.material.Typography(
    h4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 24.sp
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 20.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    ),
    subtitle2 = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    body2 = TextStyle(
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 12.sp
    )
)
