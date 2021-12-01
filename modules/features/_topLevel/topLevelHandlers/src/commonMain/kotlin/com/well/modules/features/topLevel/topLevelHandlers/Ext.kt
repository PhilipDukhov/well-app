package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.networking.userReadableDescription
import com.well.modules.utils.viewUtils.Alert

fun Alert.Error.Companion.fixDescription(t: Throwable) = Alert.Error(t, Throwable::userReadableDescription)