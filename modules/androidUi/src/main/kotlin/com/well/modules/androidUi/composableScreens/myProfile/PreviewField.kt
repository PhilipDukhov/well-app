package com.well.modules.androidUi.composableScreens.myProfile

import com.well.modules.androidUi.R
import com.well.modules.androidUi.components.ActionButton
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.myProfile.myProfileFeature.helpers.UIPreviewField
import com.well.modules.models.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun <Msg> PreviewField(
    field: UIPreviewField,
    modifier: Modifier,
    listener: (Msg) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        if (field.title.isNotBlank()) {
            Text(
                text = field.title,
                color = Color.LightGray.toColor(),
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        when (val content = field.content) {
            is UIPreviewField.Content.Text -> {
                Text(content.text)
            }
            is UIPreviewField.Content.TextAndIcon -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        content.icon.painter(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.LightBlue.toColor()),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(content.text)
                }
            }
            is UIPreviewField.Content.List -> {
                FlowRow(
                    mainAxisSpacing = 7.dp,
                    crossAxisSpacing = 7.dp,
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
            is UIPreviewField.Content.Button<*> -> {
                ActionButton(
                    enabled = content.enabled,
                    onClick = {
                        @Suppress("UNCHECKED_CAST")
                        listener(content.msg as Msg)
                    }
                ) {
                    Text(content.title)
                }
            }
        }
    }
}

@Composable
private fun UIPreviewField.Icon.painter() = when (this) {
    UIPreviewField.Icon.Location -> {
        rememberVectorPainter(Icons.Default.LocationOn)
    }
    UIPreviewField.Icon.Publications -> {
        rememberVectorPainter(Icons.Rounded.TextSnippet)
    }
    UIPreviewField.Icon.Twitter -> {
        painterResource(R.drawable.ic_twitter)
    }
    UIPreviewField.Icon.Doximity -> {
        painterResource(R.drawable.ic_doximity)
    }
}