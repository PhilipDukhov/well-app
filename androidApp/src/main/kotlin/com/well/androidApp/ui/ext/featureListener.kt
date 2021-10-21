package com.well.androidApp.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import io.github.aakira.napier.Napier

@Composable
fun <State, Msg, Eff> featureListener(
    state: MutableState<State>,
    reducer: (Msg, State) -> Pair<State, Set<Eff>>,
    effHandler: (Eff) -> Unit,
): (Msg) -> Unit = remember(state, reducer) {
    { msg ->
        val (newState, effs) = reducer(msg, state.value)
        state.value = newState
        Napier.d("featureListener $msg $newState")
        effs.forEach { eff ->
            effHandler(eff)
        }
    }
}