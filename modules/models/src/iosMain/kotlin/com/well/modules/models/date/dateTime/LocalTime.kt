@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.well.modules.models.date.dateTime

import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import kotlinx.datetime.LocalTime as KLocalTime
import kotlinx.serialization.Serializable

@Serializable(with = LocalTimeAsStringSerializer::class)
actual class LocalTime private constructor(private val delegate: KLocalTime) : Comparable<LocalTime> {

	actual val hour: Int get() = delegate.hour
	actual val minute: Int get() = delegate.minute


	actual constructor(hour: Int, minute: Int) :
		this(KLocalTime.of(hour, minute, 0, 0))


	actual override operator fun compareTo(other: LocalTime): Int =
		delegate.compareTo(other.delegate)


	override fun equals(other: Any?): Boolean =
		this === other || (other is LocalTime && delegate.equals(other.delegate))


	override fun hashCode(): Int =
		delegate.hashCode()


	actual override fun toString(): String =
		delegate.toString()


	actual companion object {
		actual val min: LocalTime = LocalTime(KLocalTime.MIN)
		actual val max: LocalTime = LocalTime(KLocalTime.MAX)

		actual fun parse(isoString: String): LocalTime =
			LocalTime(KLocalTime.parse(isoString))
	}
}
