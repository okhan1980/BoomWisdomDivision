package com.jomar.boomwisdomdivision.core.util

/**
 * A sealed class representing the result of an operation that can either succeed or fail.
 * 
 * This class encapsulates the result of operations that might fail, providing a type-safe
 * way to handle success and error states without relying on exceptions for control flow.
 * It follows functional programming principles and makes error handling explicit.
 * 
 * @param T The type of data returned on success
 */
sealed class Result<out T> {
    
    /**
     * Represents a successful operation result.
     * 
     * @property data The data returned by the successful operation
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Represents a failed operation result.
     * 
     * @property exception The exception that caused the operation to fail
     */
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    /**
     * Represents a loading state for operations that might take time.
     * This is useful for UI state management during async operations.
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this result represents a successful operation.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this result represents a failed operation.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns true if this result represents a loading state.
     */
    val isLoading: Boolean
        get() = this is Loading

    /**
     * Returns the data if this is a Success, or null if this is an Error or Loading.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> null
    }

    /**
     * Returns the data if this is a Success, or throws the exception if this is an Error.
     * Throws IllegalStateException if this is Loading.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Cannot get data from Loading state")
    }

    /**
     * Returns the data if this is a Success, or the default value otherwise.
     * 
     * @param defaultValue The value to return if this is not a Success
     */
    fun getOrDefault(defaultValue: T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }

    /**
     * Returns the exception if this is an Error, or null otherwise.
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
    }
}

/**
 * Transforms a [Result] by applying a function to the success value.
 * 
 * @param transform Function to apply to the success value
 * @return A new [Result] with the transformed value, or the original error/loading state
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Transforms a [Result] by applying a function that returns another [Result].
 * This is useful for chaining operations that might fail.
 * 
 * @param transform Function that takes the success value and returns a new [Result]
 * @return The result of the transform function, or the original error/loading state
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Executes a function if this is a Success.
 * 
 * @param action Function to execute with the success value
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Executes a function if this is an Error.
 * 
 * @param action Function to execute with the error exception
 */
inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * Executes a function if this is Loading.
 * 
 * @param action Function to execute for loading state
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        action()
    }
    return this
}

/**
 * Creates a successful Result.
 * 
 * @param data The data to wrap in a Success result
 */
fun <T> Result.Companion.success(data: T): Result<T> = Result.Success(data)

/**
 * Creates an error Result.
 * 
 * @param exception The exception to wrap in an Error result
 */
fun <T> Result.Companion.error(exception: Throwable): Result<T> = Result.Error(exception)

/**
 * Creates an error Result with a message.
 * 
 * @param message The error message
 */
fun <T> Result.Companion.error(message: String): Result<T> = 
    Result.Error(Exception(message))

/**
 * Creates a loading Result.
 */
fun <T> Result.Companion.loading(): Result<T> = Result.Loading

/**
 * Companion object for Result factory methods.
 */
object ResultCompanion {
    /**
     * Executes a suspending function and wraps the result in a [Result].
     * Catches any exceptions and returns them as [Result.Error].
     * 
     * @param block The suspending function to execute
     * @return [Result.Success] with the result, or [Result.Error] if an exception occurred
     */
    suspend inline fun <T> runCatching(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
}