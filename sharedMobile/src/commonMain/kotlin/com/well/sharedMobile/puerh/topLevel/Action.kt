package com.well.sharedMobile.puerh.topLevel

data class Action(val title: String, val block: () -> Unit = {})