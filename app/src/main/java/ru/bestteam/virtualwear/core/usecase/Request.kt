package ru.bestteam.virtualwear.core.usecase

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Polymorphic
@Serializable
abstract class Request {
    abstract fun validate(): Boolean
}
