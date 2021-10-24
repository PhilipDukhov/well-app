package com.well.modules.viewHelpers

data class Action(val title: String, val block: () -> Unit = {})
data class SuspendAction(val title: String, val block: suspend () -> Unit = {})