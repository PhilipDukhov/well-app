package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.models.User

internal class SessionInfo(val uid: User.Id): CloseableContainerImpl()