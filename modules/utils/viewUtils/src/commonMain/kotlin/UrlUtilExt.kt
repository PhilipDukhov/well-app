package com.well.modules.utils.viewUtils

import com.well.modules.utils.kotlinUtils.ifTrueOrNull

fun UrlUtil.urlIfValidOrNull(url: String) = ifTrueOrNull(isValidUrl(url)) { url }