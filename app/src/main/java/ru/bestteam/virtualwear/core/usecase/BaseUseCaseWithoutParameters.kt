package ru.bestteam.virtualwear.core.usecase

abstract class BaseUseCaseWithoutParameters<T> : BaseUseCase<EmptyRequest, T>() {
    suspend fun execute() = execute(EmptyRequest())
}
