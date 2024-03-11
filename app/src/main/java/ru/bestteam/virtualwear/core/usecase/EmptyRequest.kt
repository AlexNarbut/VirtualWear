package ru.bestteam.virtualwear.core.usecase

import kotlinx.serialization.Serializable

@Serializable
class EmptyRequest : Request() {
    override fun validate(): Boolean = true
}
