package com.well.sharedMobile

import com.well.sharedMobile.leafs.SharingScreen
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