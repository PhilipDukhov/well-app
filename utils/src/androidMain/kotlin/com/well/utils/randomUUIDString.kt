package com.well.utils

import java.util.UUID

actual fun randomUUIDString() = UUID.randomUUID().toString()
