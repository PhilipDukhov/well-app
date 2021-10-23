package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.CloseableContainer
import com.well.modules.models.UserId

internal class SessionInfo(val uid: UserId): CloseableContainer()