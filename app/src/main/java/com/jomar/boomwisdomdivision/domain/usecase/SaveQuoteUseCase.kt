package com.jomar.boomwisdomdivision.domain.usecase

import com.jomar.boomwisdomdivision.core.util.Result
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.jomar.boomwisdomdivision.core.di.IoDispatcher

/**
 * Use case for saving a quote to the user's favorites collection.
 * 
 * This use case handles the business logic for saving quotes to local storage.
 * It ensures that quotes are properly validated before saving and handles
 * edge cases such as duplicate saves and storage limits.
 * 
 * The use case operates on the IO dispatcher to ensure database operations
 * don't block the main thread.
 * 
 * @property repository The repository for quote data operations
 * @property ioDispatcher The coroutine dispatcher for IO operations
 */
class SaveQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    /**
     * Executes the use case to save a quote to favorites.
     * 
     * This operation persists the quote to local storage with a timestamp
     * indicating when it was saved. The operation includes business logic
     * validation and error handling.
     * 
     * Business rules applied:
     * - The quote must be valid (non-empty content, valid ID)
     * - If the quote is already saved, this operation is idempotent
     * - The saved quote gets a timestamp for when it was added to favorites
     * - The operation should fail gracefully if storage limits are reached
     * 
     * @param quote The quote to save to favorites
     * @return [Result] containing the saved [Quote] with updated timestamp on success,
     *         or an error with details on failure
     */
    suspend operator fun invoke(quote: Quote): Result<Quote> = withContext(ioDispatcher) {
        try {
            // Validate the quote before attempting to save
            if (!isValidQuote(quote)) {
                return@withContext Result.Error(
                    IllegalArgumentException("Cannot save invalid quote")
                )
            }
            
            // Check if the quote is already saved to avoid unnecessary operations
            when (val isAlreadySaved = repository.isQuoteSaved(quote.id)) {
                is Result.Success -> {
                    if (isAlreadySaved.data) {
                        // Quote is already saved, return it with existing saved timestamp
                        when (val existingQuote = repository.getQuoteById(quote.id)) {
                            is Result.Success -> {
                                return@withContext Result.Success(existingQuote.data)
                            }
                            is Result.Error -> {
                                // Fallback: continue with save operation
                            }
                            is Result.Loading -> {
                                return@withContext Result.Error(
                                    IllegalStateException("Unexpected loading state")
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    // Continue with save operation even if check failed
                }
                is Result.Loading -> {
                    return@withContext Result.Error(
                        IllegalStateException("Unexpected loading state in use case")
                    )
                }
            }
            
            // Mark the quote as saved with current timestamp
            val quoteToSave = if (quote.savedAt == null) {
                quote.markAsSaved(System.currentTimeMillis())
            } else {
                quote
            }
            
            // Attempt to save the quote
            when (val result = repository.saveQuote(quoteToSave)) {
                is Result.Success -> {
                    Result.Success(result.data)
                }
                is Result.Error -> {
                    Result.Error(result.exception)
                }
                is Result.Loading -> {
                    Result.Error(
                        IllegalStateException("Unexpected loading state in use case")
                    )
                }
            }
        } catch (exception: Exception) {
            // Catch any unexpected exceptions and wrap them in a Result.Error
            Result.Error(exception)
        }
    }
    
    /**
     * Validates that a quote meets the requirements for saving.
     * 
     * @param quote The quote to validate
     * @return true if the quote can be saved, false otherwise
     */
    private fun isValidQuote(quote: Quote): Boolean {
        return quote.id.isNotBlank() &&
               quote.content.isNotBlank() &&
               quote.content.length >= MIN_QUOTE_LENGTH &&
               quote.content.length <= MAX_QUOTE_LENGTH &&
               quote.author.isNotBlank()
    }
    
    companion object {
        /**
         * Minimum quote length required for saving.
         */
        private const val MIN_QUOTE_LENGTH = 1
        
        /**
         * Maximum quote length allowed for saving.
         */
        private const val MAX_QUOTE_LENGTH = 1000
    }
}