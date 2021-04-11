package com.well.modules.utils

import platform.Foundation.NSUUID

actual fun randomUUIDString() = NSUUID().UUIDString