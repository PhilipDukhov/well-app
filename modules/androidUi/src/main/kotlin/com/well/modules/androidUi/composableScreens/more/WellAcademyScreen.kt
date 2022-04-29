package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.components.NavigationBar
import com.well.modules.androidUi.theme.body1Light
import com.well.modules.features.more.moreFeature.subfeatures.WellAcademyFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.WellAcademyFeature.State
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.WellAcademyScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(title = state.title)
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(10.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Text(
            state.text,
            style = MaterialTheme.typography.body1Light,
        )
        state.items.forEach { item ->
            val context = LocalContext.current
            val drawableId = remember {
                context.resources.getIdentifier(
                    "ic_well_academy${
                        item.name.toList().joinToString("") {
                            if (it.isUpperCase()) {
                                "_$it".lowercase()
                            } else {
                                it.toString()
                            }
                        }
                    }",
                    "drawable",
                    context.packageName
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(10.dp)
                    .alpha(0.5f)
            ) {
                Icon(
                    painterResource(id = drawableId),
                    contentDescription = null,
                )
                Text(
                    item.title,
                    style = MaterialTheme.typography.body1Light,
                )
            }
        }
    }
}