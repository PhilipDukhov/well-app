package com.well.androidApp.ui.composableScreens.myProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.myProfile.UIPreviewField

@Composable
fun PreviewField(
    field: UIPreviewField,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = field.title,
            color = Color.LightGray.toColor(),
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(5.dp))
        val content = field.content
        when (content) {
            is UIPreviewField.Content.Text -> {
                Text(content.text)
            }
            is UIPreviewField.Content.TextAndIcon -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painterResource(id = content.icon.drawable),
                        colorFilter = ColorFilter.tint(Color.LightBlue.toColor()),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(content.text)
                }
            }
            is UIPreviewField.Content.List -> {
                RowColumnGrid(
                    modifier = Modifier
                ) {
                    content.list.forEach {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = Color.LightBlue15.toColor(),
                            contentColor = Color.BlackP.toColor(),
                        ) {
                            Text(
                                it,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(vertical = 3.dp, horizontal = 7.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val UIPreviewField.Icon.drawable: Int
    get() = when (this) {
        UIPreviewField.Icon.Location -> R.drawable.ic_outline_location_on_24
        UIPreviewField.Icon.Publications -> R.drawable.ic_round_text_snippet_24
        UIPreviewField.Icon.Twitter -> R.drawable.ic_twitter
        UIPreviewField.Icon.Doximity -> R.drawable.ic_doximity
    }