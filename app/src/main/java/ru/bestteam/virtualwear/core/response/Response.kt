package ru.bestteam.virtualwear.core.response

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import ru.bestteam.virtualwear.core.serialization.NotSerializable

@Serializable
sealed class Response<out T> {
    @Serializable
    class Success<out T>(val value: T) : Response<T>()

    @Serializable(with = NotSerializable::class)
    sealed class Error : Response<Nothing>() {
        abstract val exception: Throwable
        abstract val message: String?
        fun getCode() = exception::class.simpleName // its need for flutter error channel

        @Serializable
        class Network(
            @Polymorphic
            override val exception: Throwable,
            override val message: String? = null,
        ) : Error()

        @Serializable
        class Bluetooth(
            @Polymorphic
            override val exception: Throwable,
            override val message: String? = null,
        ) : Error()

        @Serializable
        class Wifi(
            @Polymorphic
            override val exception: Throwable,
            override val message: String? = null,
        ) : Error()

        @Serializable
        class General(
            @Polymorphic
            override val exception: Throwable,
            @Serializable
            override val message: String? = null,
        ) : Error()
    }
}
