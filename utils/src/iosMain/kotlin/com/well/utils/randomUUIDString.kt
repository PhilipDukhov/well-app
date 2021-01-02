package com.well.utils

import platform.Foundation.NSUUID

actual fun randomUUIDString() = NSUUID().UUIDString