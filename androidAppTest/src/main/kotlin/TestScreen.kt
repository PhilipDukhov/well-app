package com.well.androidAppTest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
inline fun <State, Msg, Eff>TestScreenReducerView(
    initial: State,
    crossinline reducer: (Msg, State) -> Pair<State, Set<Eff>>,
    screen: @Composable (State, (Msg) -> Unit) -> Unit,
)  {
    var state by remember {
        mutableStateOf(initial)
    }
    screen(state) {
        state = reducer(it, state).first
        println("reduced $state")
    }
}