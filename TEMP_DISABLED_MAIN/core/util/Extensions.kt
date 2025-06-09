package com.jomar.boomwisdomdivision.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Extension functions for common operations throughout the Boom Wisdom Division app.
 * 
 * This file contains utility extension functions that provide convenient methods
 * for common operations like data transformation, error handling, and validation.
 */

// String Extensions

/**
 * Checks if a string is a valid quote content.
 * A valid quote should not be blank and should be within reasonable length limits.
 * 
 * @return true if the string is valid quote content, false otherwise
 */
fun String.isValidQuoteContent(): Boolean {
    return this.isNotBlank() && 
           this.length >= Constants.MIN_QUOTE_LENGTH && 
           this.length <= Constants.MAX_QUOTE_LENGTH
}

/**
 * Truncates a string to a maximum length and adds ellipsis if necessary.
 * 
 * @param maxLength The maximum length of the resulting string
 * @param ellipsis The string to append when truncation occurs (default: "...")
 * @return The truncated string with ellipsis if necessary
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Capitalizes the first letter of each sentence in the string.
 * Useful for ensuring proper capitalization in quote content.
 * 
 * @return String with properly capitalized sentences
 */
fun String.capitalizeSentences(): String {
    return this.split(". ").joinToString(". ") { sentence ->
        sentence.trim().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}

/**
 * Removes extra whitespace and normalizes line breaks in quote text.
 * 
 * @return Cleaned and normalized string
 */
fun String.normalizeQuoteText(): String {
    return this.trim()
        .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
        .replace(Regex("\\n\\s*\\n"), "\n\n") // Normalize paragraph breaks
        .trim()
}

// List Extensions

/**
 * Safely gets a random element from the list.
 * 
 * @return A random element from the list, or null if the list is empty
 */
fun <T> List<T>.randomOrNull(): T? {
    return if (isEmpty()) null else random()
}

/**
 * Chunks a list into smaller lists of specified size.
 * 
 * @param size The maximum size of each chunk
 * @return List of lists, each containing at most [size] elements
 */
fun <T> List<T>.chunked(size: Int): List<List<T>> {
    return if (size <= 0) {
        throw IllegalArgumentException("Chunk size must be positive")
    } else {
        (0 until this.size step size).map { i ->
            this.subList(i, minOf(i + size, this.size))
        }
    }
}

// Flow Extensions

/**
 * Wraps a Flow in a Result wrapper, handling errors gracefully.
 * Emits Loading initially, then Success for each emission, or Error if an exception occurs.
 * 
 * @return Flow that emits Result wrapper around the original values
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }
}

/**
 * Maps a Flow to emit only Success results, filtering out errors and loading states.
 * 
 * @return Flow that emits only the data from successful Results
 */
fun <T> Flow<Result<T>>.mapSuccess(): Flow<T> {
    return this.map { result ->
        when (result) {
            is Result.Success -> result.data
            is Result.Error -> throw result.exception
            is Result.Loading -> throw IllegalStateException("Cannot map loading state")
        }
    }
}

// Long Extensions (for timestamps)

/**
 * Formats a timestamp as a human-readable relative time string.
 * 
 * @return String representing the relative time (e.g., "2 hours ago", "3 days ago")
 */
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> "${diff / 86_400_000} days ago"
        diff < 2_592_000_000 -> "${diff / 604_800_000} weeks ago"
        else -> "${diff / 2_592_000_000} months ago"
    }
}

/**
 * Formats a timestamp as a simple date string.
 * 
 * @return Date string in "MMM dd, yyyy" format
 */
fun Long.toDateString(): String {
    val date = java.util.Date(this)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

// Exception Extensions

/**
 * Provides a user-friendly error message for common exceptions.
 * 
 * @return Human-readable error message appropriate for displaying to users
 */
fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is java.net.UnknownHostException,
        is java.net.ConnectException,
        is java.net.SocketTimeoutException -> Constants.ERROR_NETWORK
        is java.io.IOException -> "Network error occurred. Please try again."
        is IllegalArgumentException -> "Invalid data provided."
        is IllegalStateException -> "App is in an invalid state. Please restart."
        else -> this.message ?: "An unexpected error occurred."
    }
}

// Boolean Extensions

/**
 * Executes the given action if the boolean is true.
 * 
 * @param action The action to execute
 * @return The original boolean value
 */
inline fun Boolean.ifTrue(action: () -> Unit): Boolean {
    if (this) action()
    return this
}

/**
 * Executes the given action if the boolean is false.
 * 
 * @param action The action to execute
 * @return The original boolean value
 */
inline fun Boolean.ifFalse(action: () -> Unit): Boolean {
    if (!this) action()
    return this
}

// Collection Extensions

/**
 * Checks if a collection is not null and not empty.
 * 
 * @return true if the collection is not null and not empty
 */
fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

/**
 * Returns the collection if it's not null and not empty, otherwise returns null.
 * 
 * @return The collection if valid, null otherwise
 */
fun <T> Collection<T>?.takeIfNotEmpty(): Collection<T>? {
    return if (isNotNullOrEmpty()) this else null
}

// Nullable Extensions

/**
 * Returns the value if it's not null, otherwise executes the given function and returns its result.
 * 
 * @param defaultValue Function that provides the default value
 * @return The original value if not null, or the result of defaultValue function
 */
inline fun <T> T?.orElse(defaultValue: () -> T): T {
    return this ?: defaultValue()
}
