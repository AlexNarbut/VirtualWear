package ru.bestteam.virtualwear.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NotSerializable : KSerializer<Any> {
    private val exception = SerializationException(
        "Types annotated as `@Serializable( with = NotSerializable::class )` are never expected to be serialized. " +
            "The serializer is only defined since the compiler does not know this, causing a compilation error."
    )

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("This should never be serialized.")

    override fun deserialize(decoder: Decoder): Any = throw exception
    override fun serialize(encoder: Encoder, value: Any) = throw exception
}
