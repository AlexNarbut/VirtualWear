package ru.bestteam.virtualwear.core.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.bestteam.virtualwear.core.response.Response

abstract class BaseFlowableUseCase<R : Request, T> {
    open val methodName = this::class.simpleName.toString()

    open suspend fun execute(request: R): Flow<Response<T>> {
        val validated = request.validate()
        if (validated) return run(request)
        return flow { Response.Error.General(IllegalArgumentException("Param is not valid")) }
    }

    protected abstract suspend fun run(request: R): Flow<Response<T>>
}
