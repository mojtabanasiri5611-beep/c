package com.example.core.common

/**
 * A generic class that holds a value or an exception for domain/data layer operations.
 */
sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val exception: Throwable, val message: String? = exception.message) : AppResult<Nothing>()
    object Loading : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    inline fun onSuccess(action: (value: T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (exception: Throwable, message: String?) -> Unit): AppResult<T> {
        if (this is Error) action(exception, message)
        return this
    }

    inline fun <R> map(transform: (value: T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception, message)
        is Loading -> Loading
    }
}
