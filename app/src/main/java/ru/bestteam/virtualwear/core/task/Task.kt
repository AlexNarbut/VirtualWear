package ru.bestteam.virtualwear.core.task

import com.google.android.gms.tasks.Task
import ru.bestteam.virtualwear.core.response.Response

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                task.exception ?: RuntimeException("Unknown task exception")
            )
        }
    }
}

suspend fun <T> Task<T>.awaitResponse(): Response<T> = suspendCoroutine { continuation ->
    addOnSuccessListener {
        continuation.resume(Response.Success(it))
    }
    addOnFailureListener {
        continuation.resume(
            Response.Error.General(it)
        )
    }
}