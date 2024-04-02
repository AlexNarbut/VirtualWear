package ru.bestteam.virtualwear.app.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel() : ViewModel() {
    protected open val errorHandler = CoroutineExceptionHandler { context, exception ->
        processCriticalError(exception)
    }

    protected open fun processCriticalError(exception: Throwable) = Unit

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(errorHandler) {
            block.invoke(this)
        }
    }

    protected fun safeLaunch(
        coroutineContext: CoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val context = if (coroutineContext[CoroutineExceptionHandler.Key] != null) coroutineContext
        else coroutineContext + errorHandler

        return viewModelScope.launch(context) {
            block.invoke(this)
        }
    }
}