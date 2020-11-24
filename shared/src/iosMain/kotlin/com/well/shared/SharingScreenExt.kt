package com.well.shared

import com.well.shared.leafs.SharingScreen
import oolong.Dispatch
import oolong.runtime

@Suppress("unused")
fun SharingScreen.runtime(userId: String, render: (SharingScreen.Props, Dispatch<SharingScreen.Msg>) -> Unit) =
    runtime(
        init(userId),
        update,
        view,
        render,
        MainLoopDispatcher,
        MainLoopDispatcher,
        MainLoopDispatcher
    )