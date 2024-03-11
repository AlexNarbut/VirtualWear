package ru.bestteam.virtualwear.core.usecase

import kotlinx.coroutines.flow.Flow
import ru.bestteam.virtualwear.core.response.Response

suspend inline operator fun <R : Request, T> BaseUseCase<R, T>.invoke(request: R): Response<T> =
    execute(request)

suspend inline operator fun <T> BaseUseCaseWithoutParameters<T>.invoke(): Response<T> = execute()

suspend inline operator fun <R : Request, T> BaseFlowableUseCase<R, T>.invoke(request: R): Flow<Response<T>> =
    execute(request)

suspend inline operator fun <T> BaseFlowableUseCaseWithoutParameters<T>.invoke(): Flow<Response<T>> =
    execute()
