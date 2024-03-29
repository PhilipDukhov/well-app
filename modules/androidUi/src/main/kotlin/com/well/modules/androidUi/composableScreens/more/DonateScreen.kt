package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.components.ActionButton
import com.well.modules.androidUi.components.ControlItem
import com.well.modules.androidUi.components.NavigationBar
import com.well.modules.androidUi.components.SelectableButton
import com.well.modules.androidUi.components.SwitchRow
import com.well.modules.androidUi.theme.body1Light
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature.State
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature as Feature
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.DonateScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(
        title = Feature.Strings.title,
        leftItem = ControlItem.back { listener(Msg.Back) }
    )
    var selectedVariantIndex by rememberSaveable { mutableStateOf(0) }
    var isRecurring by rememberSaveable { mutableStateOf(true) }
    Column(
        Modifier.padding(horizontal = 17.dp)
    ) {
        Spacer(Modifier.weight(34f))
        Text(
            Feature.Strings.text,
            style = MaterialTheme.typography.body1Light,
        )
        Spacer(Modifier.weight(38f))
        Text(
            Feature.Strings.howMuch,
            style = MaterialTheme.typography.subtitle2,
        )
        Spacer(Modifier.weight(38f))
        Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
            state.variants.forEachIndexed { i, variant ->
                SelectableButton(
                    selected = selectedVariantIndex == i,
                    onClick = {
                        selectedVariantIndex = i
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "\$${variant.price}",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }
        Spacer(Modifier.weight(31f))
        SwitchRow(
            text = Feature.Strings.isRecurring,
            checked = isRecurring,
            onCheckedChange = { isRecurring = !isRecurring },
        )
        Spacer(Modifier.weight(50f))
        ActionButton(
            onClick = {
                listener(Msg.Donate(variant = state.variants[selectedVariantIndex], isRecurring = isRecurring))
            }
        ) {
            Text(Feature.Strings.`continue`)
        }
        Spacer(Modifier.weight(400f))
    }
}