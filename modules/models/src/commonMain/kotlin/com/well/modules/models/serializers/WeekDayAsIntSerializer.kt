package com.well.modules.models.serializers

import io.ktor.util.date.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = WeekDay::class)
object WeekDayAsIntSerializer : KSerializer<WeekDay> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "WeekDay",
        kind = PrimitiveKind.INT
    )

    override fun serialize(encoder: Encoder, value: WeekDay) {
        encoder.encodeInt(value.ordinal)
    }

    override fun deserialize(decoder: Decoder): WeekDay {
        return WeekDay.from(decoder.decodeInt())
    }
}