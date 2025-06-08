package com.jomar.boomwisdomdivision.domain.usecase

import com.jomar.boomwisdomdivision.core.util.Result
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.jomar.boomwisdomdivision.core.di.IoDispatcher

/**
 * Use case for fetching a random motivational quote.
 * 
 * This use case handles the business logic for retrieving random quotes from the data layer.
 * It encapsulates the interaction with the QuoteRepository and provides a clean interface
 * for the presentation layer to fetch quotes without knowing about the underlying data sources.
 * 
 * The use case operates on the IO dispatcher to ensure network operations don't block the main thread.
 * 
 * @property repository The repository for quote data operations
 * @property ioDispatcher The coroutine dispatcher for IO operations
 */
class GetRandomQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    /**
     * Executes the use case to fetch a random quote.
     * 
     * This operation retrieves a random quote from the remote API through the repository.
     * The operation is performed on the IO dispatcher to avoid blocking the calling thread.
     * 
     * Business rules applied:
     * - The quote content must be valid (not empty, within length limits)
     * - If the API returns an invalid quote, the operation should retry or return an error
     * - Network errors are handled gracefully and appropriate error messages are provided
     * 
     * @return [Result] containing a [Quote] on success, or an error with details on failure
     */
    suspend operator fun invoke(): Result<Quote> = withContext(ioDispatcher) {
        try {
            when (val result = repository.getRandomQuote()) {
                is Result.Success -> {
                    val quote = result.data
                    
                    // Validate the quote content according to business rules
                    if (!isValidQuote(quote)) {
                        Result.Error(
                            IllegalStateException("Received invalid quote from data source")
                        )
                    } else {
                        Result.Success(quote)
                    }
                }
                is Result.Error -> {
                    // Log the error for debugging purposes (in a real app, use proper logging)
                    Result.Error(result.exception)
                }
                is Result.Loading -> {
                    // This shouldn't happen in a suspend function, but handle it gracefully
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
     * Validates that a quote meets the business requirements.
     * 
     * @param quote The quote to validate
     * @return true if the quote is valid, false otherwise
     */
    private fun isValidQuote(quote: Quote): Boolean {
        return quote.content.isNotBlank() &&
               quote.content.length >= MIN_QUOTE_LENGTH &&
               quote.content.length <= MAX_QUOTE_LENGTH &&
               quote.author.isNotBlank() &&
               quote.id.isNotBlank()
    }
    
    companion object {
        /**
         * Minimum acceptable quote length for display.
         */
        private const val MIN_QUOTE_LENGTH = 10
        
        /**
         * Maximum acceptable quote length for display.
         */
        private const val MAX_QUOTE_LENGTH = 500
    }
}