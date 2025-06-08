package com.jomar.boomwisdomdivision.domain.repository

import com.jomar.boomwisdomdivision.core.util.Result
import com.jomar.boomwisdomdivision.domain.model.Quote
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quote data operations.
 * 
 * This interface defines the contract for quote data access, following the Repository pattern
 * and Clean Architecture principles. It abstracts away the data source implementation details
 * and provides a clean API for the domain layer to interact with quote data.
 * 
 * The implementation will handle coordination between remote API and local database,
 * ensuring proper caching, offline support, and error handling.
 */
interface QuoteRepository {

    /**
     * Fetches a random quote from the remote data source.
     * 
     * This operation attempts to retrieve a fresh quote from the API. If the network
     * request fails, it may fall back to a cached quote depending on the implementation.
     * 
     * @return [Result] containing a random [Quote] on success, or an error on failure
     */
    suspend fun getRandomQuote(): Result<Quote>

    /**
     * Saves a quote to the local favorites collection.
     * 
     * This operation persists the quote to local storage and marks it as saved
     * with the current timestamp. If the quote is already saved, this operation
     * should be idempotent.
     * 
     * @param quote The quote to save to favorites
     * @return [Result] containing the saved [Quote] with updated savedAt timestamp,
     *         or an error if the operation fails
     */
    suspend fun saveQuote(quote: Quote): Result<Quote>

    /**
     * Removes a quote from the local favorites collection.
     * 
     * This operation removes the quote from local storage. If the quote is not
     * currently saved, this operation should be idempotent.
     * 
     * @param quoteId The unique identifier of the quote to remove
     * @return [Result] containing Unit on success, or an error if the operation fails
     */
    suspend fun deleteQuote(quoteId: String): Result<Unit>

    /**
     * Retrieves all saved quotes from local storage as a reactive stream.
     * 
     * This returns a Flow that emits the current list of saved quotes and will
     * automatically emit updated lists when quotes are added or removed from favorites.
     * The quotes are typically ordered by savedAt timestamp in descending order
     * (most recently saved first).
     * 
     * @return [Flow] of [List] containing all saved quotes, ordered by save date
     */
    fun getSavedQuotes(): Flow<List<Quote>>

    /**
     * Checks if a specific quote is currently saved to favorites.
     * 
     * This is a convenience method for checking the saved status of a quote
     * without retrieving the entire saved quotes collection.
     * 
     * @param quoteId The unique identifier of the quote to check
     * @return [Result] containing true if the quote is saved, false otherwise,
     *         or an error if the operation fails
     */
    suspend fun isQuoteSaved(quoteId: String): Result<Boolean>

    /**
     * Retrieves a specific quote by its unique identifier.
     * 
     * This method first checks the local database for the quote, and if not found,
     * may attempt to fetch it from the remote source depending on the implementation.
     * 
     * @param quoteId The unique identifier of the quote to retrieve
     * @return [Result] containing the [Quote] if found, or an error if not found or operation fails
     */
    suspend fun getQuoteById(quoteId: String): Result<Quote>
}