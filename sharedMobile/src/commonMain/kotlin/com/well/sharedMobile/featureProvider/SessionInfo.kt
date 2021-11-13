package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.CloseableContainer
import com.well.modules.models.User

internal class SessionInfo(val uid: User.Id): CloseableContainer()