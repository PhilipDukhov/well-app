package com.well.modules.models.date.dateTime

import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable(with = LocalTimeAsStringSerializer::class)
expect class LocalTime(
    hour: Int,
    minute: Int,
) : Comparable<LocalTime> {
    val hour: Int
    val minute: Int

    override operator fun compareTo(other: LocalTime): Int
    override fun toString(): String

    companion object {

        /**
         * Parses a string that represents a time value in ISO-8601 format and returns the parsed [LocalTime] value.
         *
         * Examples of date/time in ISO-8601 format:
         * - `18:43`
         * - `18:43:00`
         * - `18:43:00.500`
         * - `18:43:00.123456789`
         *
         * @throws IllegalArgumentException if the text cannot be parsed or the boundaries of [LocalTime] are exceeded.
         */
        fun parse(isoString: String): LocalTime

        val min: LocalTime
        val max: LocalTime
    }
}

