package com.well.modules.models.date.dateTime

import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import java.time.LocalTime as jtLocalTime
import java.time.*
import java.time.format.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable(with = LocalTimeAsStringSerializer::class)
actual class LocalTime(internal val value: jtLocalTime) : Comparable<LocalTime> {

	actual constructor(hour: Int, minute: Int) :
		this(try {
			jtLocalTime.of(hour, minute)
		}
		catch (e: DateTimeException) {
			throw IllegalArgumentException(e)
		})

	actual val hour: Int get() = value.hour
	actual val minute: Int get() = value.minute

	override fun equals(other: Any?): Boolean =
		(this === other) || (other is LocalTime && this.value == other.value)

	override fun hashCode(): Int = value.hashCode()

	actual override fun toString(): String = value.toString()

	actual override fun compareTo(other: LocalTime): Int = this.value.compareTo(other.value)


	actual companion object {

		actual fun parse(isoString: String): LocalTime =
			try {
				jtLocalTime.parse(isoString).let(::LocalTime)
			}
			catch (e: DateTimeParseException) {
				@Suppress("INVISIBLE_MEMBER")
				throw DateTimeFormatException(e)
			}

		actual val min: LocalTime = LocalTime(jtLocalTime.MIN)
		actual val max: LocalTime = LocalTime(jtLocalTime.MAX)
	}
}

fun LocalTime.toJavaLocalTime(): java.time.LocalTime = this.value
