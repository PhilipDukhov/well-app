package com.well.modules.utils.dbUtils

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object InstantColumnAdapter : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}