package com.well.sharedMobile

data class Action(val title: String, val block: () -> Unit = {})
data class SuspendAction(val title: String, val block: suspend () -> Unit = {})