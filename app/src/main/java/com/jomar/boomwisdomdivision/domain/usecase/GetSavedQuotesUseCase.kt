package com.jomar.boomwisdomdivision.domain.usecase

import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.jomar.boomwisdomdivision.core.di.IoDispatcher

/**
 * Use case for retrieving all saved quotes from the user's favorites collection.
 * 
 * This use case provides a reactive stream of saved quotes, allowing the UI to automatically
 * update when quotes are added to or removed from favorites. It handles the business logic
 * for ordering and filtering the saved quotes according to user preferences.
 * 
 * The use case returns a Flow that operates on the IO dispatcher to ensure database
 * operations don't block the main thread.
 * 
 * @property repository The repository for quote data operations
 * @property ioDispatcher The coroutine dispatcher for IO operations
 */
class GetSavedQuotesUseCase @Inject constructor(
    private val repository: QuoteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    
    /**
     * Executes the use case to retrieve all saved quotes as a reactive stream.
     * 
     * This operation returns a Flow that emits the current list of saved quotes
     * and automatically updates when the favorites collection changes. The quotes
     * are processed according to business rules for ordering and validation.
     * 
     * Business rules applied:
     * - Quotes are ordered by save date (most recently saved first)
     * - Only valid quotes are included in the result
     * - The flow operates on the IO dispatcher to avoid blocking the main thread
     * - Empty lists are valid and indicate no saved quotes
     * 
     * @return [Flow] of [List] containing all saved quotes, ordered by save date descending
     */
    operator fun invoke(): Flow<List<Quote>> {
        return repository.getSavedQuotes()
            .map { quotes ->
                // Apply business logic for ordering and filtering
                quotes.asSequence()
                    .filter { isValidSavedQuote(it) } // Filter out invalid quotes
                    .sortedByDescending { it.savedAt ?: 0L } // Sort by save date, newest first
                    .take(MAX_DISPLAYED_QUOTES) // Limit the number of displayed quotes
                    .toList()
            }
            .flowOn(ioDispatcher)
    }
    
    /**
     * Executes the use case to retrieve saved quotes with a specific ordering.
     * 
     * @param sortOrder The order in which to sort the quotes
     * @return [Flow] of [List] containing saved quotes in the specified order
     */
    operator fun invoke(sortOrder: SortOrder): Flow<List<Quote>> {
        return repository.getSavedQuotes()
            .map { quotes ->
                val validQuotes = quotes.filter { isValidSavedQuote(it) }
                
                when (sortOrder) {
                    SortOrder.DATE_SAVED_DESC -> {
                        validQuotes.sortedByDescending { it.savedAt ?: 0L }
                    }
                    SortOrder.DATE_SAVED_ASC -> {
                        validQuotes.sortedBy { it.savedAt ?: 0L }
                    }
                    SortOrder.AUTHOR_NAME -> {
                        validQuotes.sortedBy { it.author.lowercase() }
                    }
                    SortOrder.CONTENT_LENGTH -> {
                        validQuotes.sortedBy { it.content.length }
                    }
                    SortOrder.ALPHABETICAL -> {
                        validQuotes.sortedBy { it.content.lowercase() }
                    }
                }.take(MAX_DISPLAYED_QUOTES)
            }
            .flowOn(ioDispatcher)
    }
    
    /**
     * Retrieves saved quotes filtered by a search query.
     * 
     * @param searchQuery The query to search for in quote content and author
     * @return [Flow] of [List] containing quotes that match the search criteria
     */
    fun searchSavedQuotes(searchQuery: String): Flow<List<Quote>> {
        return repository.getSavedQuotes()
            .map { quotes ->
                if (searchQuery.isBlank()) {
                    // Return all quotes if search is empty
                    quotes.filter { isValidSavedQuote(it) }
                        .sortedByDescending { it.savedAt ?: 0L }
                } else {
                    // Filter quotes that contain the search query
                    val query = searchQuery.lowercase().trim()
                    quotes.asSequence()
                        .filter { isValidSavedQuote(it) }
                        .filter { quote ->
                            quote.content.lowercase().contains(query) ||
                            quote.author.lowercase().contains(query) ||
                            quote.tags.any { tag -> tag.lowercase().contains(query) }
                        }
                        .sortedByDescending { it.savedAt ?: 0L }
                        .toList()
                }
            }
            .flowOn(ioDispatcher)
    }
    
    /**
     * Retrieves saved quotes filtered by tags.
     * 
     * @param tags The list of tags to filter by
     * @return [Flow] of [List] containing quotes that have any of the specified tags
     */
    fun getSavedQuotesByTags(tags: List<String>): Flow<List<Quote>> {
        return repository.getSavedQuotes()
            .map { quotes ->
                if (tags.isEmpty()) {
                    quotes.filter { isValidSavedQuote(it) }
                        .sortedByDescending { it.savedAt ?: 0L }
                } else {
                    val lowercaseTags = tags.map { it.lowercase() }
                    quotes.asSequence()
                        .filter { isValidSavedQuote(it) }
                        .filter { quote ->
                            quote.tags.any { tag -> 
                                lowercaseTags.contains(tag.lowercase()) 
                            }
                        }
                        .sortedByDescending { it.savedAt ?: 0L }
                        .toList()
                }
            }
            .flowOn(ioDispatcher)
    }
    
    /**
     * Validates that a saved quote meets the requirements for display.
     * 
     * @param quote The quote to validate
     * @return true if the quote is valid for display, false otherwise
     */
    private fun isValidSavedQuote(quote: Quote): Boolean {
        return quote.id.isNotBlank() &&
               quote.content.isNotBlank() &&
               quote.author.isNotBlank() &&
               quote.savedAt != null &&
               quote.savedAt > 0
    }
    
    /**
     * Enum representing different ways to sort saved quotes.
     */
    enum class SortOrder {
        /**
         * Sort by save date, most recent first.
         */
        DATE_SAVED_DESC,
        
        /**
         * Sort by save date, oldest first.
         */
        DATE_SAVED_ASC,
        
        /**
         * Sort alphabetically by author name.
         */
        AUTHOR_NAME,
        
        /**
         * Sort by content length, shortest first.
         */
        CONTENT_LENGTH,
        
        /**
         * Sort alphabetically by quote content.
         */
        ALPHABETICAL
    }
    
    companion object {
        /**
         * Maximum number of quotes to display in the UI to prevent performance issues.
         */
        private const val MAX_DISPLAYED_QUOTES = 1000
    }
}
