package ru.bestteam.virtualwear.core.usecase

import ru.bestteam.virtualwear.core.response.Response

abstract class BaseUseCase<R : Request, T> {

    open suspend fun execute(request: R): Response<T> {
        // this.request = request
        val validated = request.validate()
        if (validated) return run(request)
        return Response.Error.General(IllegalArgumentException("Param is not valid"))
    }

    protected abstract suspend fun run(request: R): Response<T>
}
