package com.well.server.utils

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.getPrimitiveContent(key: String) =
    getValue(key).jsonPrimitive.content