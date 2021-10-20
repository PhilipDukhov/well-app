package com.well.modules.models.serializers

import com.well.modules.models.date.dateTime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = LocalTime::class)
object LocalTimeAsStringSerializer : KSerializer<LocalTime> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "LocalTime",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString())
    }
}