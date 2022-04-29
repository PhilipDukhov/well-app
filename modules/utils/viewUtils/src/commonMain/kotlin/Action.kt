package com.well.modules.utils.viewUtils

data class Action(val title: String, val action: () -> Unit = {})
data class SuspendAction(val title: String, val action: suspend () -> Unit = {})