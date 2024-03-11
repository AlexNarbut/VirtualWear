package ru.bestteam.virtualwear.core.usecase

abstract class BaseFlowableUseCaseWithoutParameters<T> : BaseFlowableUseCase<EmptyRequest, T>() {
    suspend fun execute() = execute(EmptyRequest())
}
