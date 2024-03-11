package ru.bestteam.virtualwear.core.usecase

interface Interactor {
    val channelName get() = this::class.qualifiedName.toString()
}

abstract class BaseInteractor : Interactor {
    override val channelName = this::class.qualifiedName.toString()
}
