package ru.bestteam.virtualwear.core.response

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transform

inline fun <T> Response<T>.alsoIfSuccess(it: (T) -> Unit): Response<T> {
    if (this is Response.Success) {
        it.invoke(this.value)
    }
    return this
}

inline fun <T> Response<T>.alsoIfError(it: (Response.Error) -> Unit): Response<T> {
    if (this is Response.Error) {
        it.invoke(this)
    }
    return this
}

inline fun <T> Response<T>.getValueIfSuccess(): T? {
    if (this is Response.Success) {
        return this.value
    }
    return null
}

inline fun <T, R> Response<T>.mapIfSuccess(it: (T) -> Response<R>): Response<R> {
    return when (this) {
        is Response.Success -> it.invoke(this.value)
        is Response.Error -> this
    }
}

inline fun <T, R> Response<T>.safeMapIfSuccess(it: (T) -> Response<R>): Response<R> {
    return when (this) {
        is Response.Success -> safeResponseRun { it.invoke(this.value) }
        is Response.Error -> this
    }
}

inline fun <T> Response<T>.mapIfError(it: (Response.Error) -> Response<T>): Response<T> {
    return when (this) {
        is Response.Success -> this
        is Response.Error -> it.invoke(this)
    }
}

inline fun <T> Response<T>.mapException(mapper: (Throwable) -> Throwable): Response<T> {
    return when (this) {
        is Response.Error.Network -> Response.Error.Network(mapper(this.exception), message)
        is Response.Error.Bluetooth -> Response.Error.Bluetooth(mapper(this.exception), message)
        is Response.Error.Wifi -> Response.Error.Wifi(mapper(this.exception), message)
        is Response.Error.General -> Response.Error.General(mapper(this.exception), message)
        is Response.Success -> this
    }
}

inline fun <T, R> Response<T>.mapValueIfSuccess(it: (T) -> R): Response<R> {
    return when (this) {
        is Response.Success -> Response.Success(it.invoke(this.value))
        is Response.Error -> this
    }
}

inline fun <T> Response<T>.getExceptionIfError(): Throwable? {
    if (this is Response.Error) return exception
    return null
}

inline fun <T, R> Response<T>.safeMapValueIfSuccess(it: (T) -> R): Response<R> {
    return when (this) {
        is Response.Success -> safeRun { it.invoke(this.value) }
        is Response.Error -> this
    }
}

inline fun <R> safeRun(block: () -> R): Response<R> {
    return try {
        Response.Success(block())
    } catch (ex: CancellationException) {
        throw ex
    } catch (e: Throwable) {
        Response.Error.General(e, e.message)
    }
}

inline fun <R> safeRun(error: Response.Error, block: () -> R): Response<R> {
    return try {
        Response.Success(block())
    } catch (ex: CancellationException) {
        throw ex
    } catch (e: Throwable) {
        Response.Error.General(e, e.message)
    }
}

inline fun <R> safeResponseRun(block: () -> Response<R>): Response<R> {
    return try {
        block()
    } catch (ex: CancellationException) {
        throw ex
    } catch (e: Throwable) {
        Response.Error.General(e, e.message)
    }
}

suspend inline fun <R> safeFlowResponseRun(noinline block: suspend () -> Flow<R>): Flow<Response<R>> {
    return try {
        block()
            .transform<R, Response<R>> { emit(Response.Success(it)) }
            .catch { e -> emit(Response.Error.General(e, e.message)) }
    } catch (ex: CancellationException) {
        throw ex
    } catch (e: Throwable) {
        flowOf(Response.Error.General(e, e.message))
    }
}

suspend inline fun <R> safeFlowRun(noinline block: suspend () -> Flow<Response<R>>): Flow<Response<R>> {
    return try {
        block().catch { e ->
            emit(Response.Error.General(e, e.message))
        }
    } catch (ex: CancellationException) {
        throw ex
    } catch (e: Throwable) {
        flowOf(Response.Error.General(e, e.message))
    }
}

suspend inline fun <T> retry(
    retryCount: Int = 5,
    delay: Long = 0,
    block: suspend () -> Response<T>,
): Response<T> {
    for (currentTry in 0 until retryCount - 1) {
        val response = block()
        if (response is Response.Success) return response
        if (delay > 0) delay(delay)
    }

    return block()
}

inline fun <T> T.asResponse() = Response.Success(this)
inline fun Throwable.asGeneralError() = Response.Error.General(this, this.message)
inline fun Throwable.asNetworkError() = Response.Error.Network(this, this.message)
inline fun Throwable.asBluetoothError() = Response.Error.Bluetooth(this, this.message)
inline fun Throwable.asWifiError() = Response.Error.Wifi(this, this.message)
