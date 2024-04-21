package ru.bestteam.virtualwear.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex

fun <T> Flow<T>.takeEach(n: Int): Flow<T> {
    return withIndex()
        .filter { it.index % n == 0 }
        .map { it.value }
}

inline fun <T, R> Flow<T>.switchIf(
    flow: Flow<R>,
    crossinline predicate: (T) -> Boolean,
): Flow<R> {
    return flatMapLatest { value ->
        if (predicate(value)) {
            flow
        } else {
            emptyFlow()
        }
    }
}

inline fun <reified R> Flow<*>.switchIfInstance(): Flow<R> {
    return flatMapLatest { value ->
        if (value is R) {
            flowOf(value)
        } else {
            emptyFlow()
        }
    }
}