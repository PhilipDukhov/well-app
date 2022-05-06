package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.networking.userReadableDescription
import com.well.modules.utils.viewUtils.Alert

internal fun Alert.Error.Companion.exceptionAlert(e: Exception) = Alert.Error(e, Exception::userReadableDescription)