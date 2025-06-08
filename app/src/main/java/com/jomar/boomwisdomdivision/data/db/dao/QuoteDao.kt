package com.jomar.boomwisdomdivision.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Quote entities
 *
 * Provides database operations for managing saved quotes in the local SQLite database.
 * All operations are suspend functions to work with coroutines for asynchronous execution.
 */
@Dao
interface QuoteDao {

    /**
     * Inserts a quote into the database
     *
     * If a quote with the same ID already exists, it will be replaced.
     *
     * @param quote The QuoteEntity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    /**
     * Inserts multiple quotes into the database
     *
     * If quotes with the same IDs already exist, they will be replaced.
     *
     * @param quotes The list of QuoteEntity objects to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    /**
     * Deletes a specific quote from the database
     *
     * @param quote The QuoteEntity to delete
     */
    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    /**
     * Deletes a quote by its ID
     *
     * @param quoteId The ID of the quote to delete
     * @return The number of rows deleted (should be 1 if successful, 0 if not found)
     */
    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: String): Int

    /**
     * Retrieves all saved quotes ordered by save date (most recent first)
     *
     * Returns a Flow to observe changes in the database automatically.
     *
     * @return Flow emitting list of all QuoteEntity objects
     */
    @Query("SELECT * FROM quotes ORDER BY savedAt DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    /**
     * Retrieves a specific quote by its ID
     *
     * @param quoteId The ID of the quote to retrieve
     * @return The QuoteEntity if found, null otherwise
     */
    @Query("SELECT * FROM quotes WHERE id = :quoteId")
    suspend fun getQuoteById(quoteId: String): QuoteEntity?

    /**
     * Checks if a quote is already saved in the database
     *
     * @param quoteId The ID of the quote to check
     * @return true if the quote exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM quotes WHERE id = :quoteId)")
    suspend fun isQuoteSaved(quoteId: String): Boolean

    /**
     * Searches for quotes by author name (case-insensitive)
     *
     * @param authorName The author name to search for
     * @return Flow emitting list of matching QuoteEntity objects
     */
    @Query("SELECT * FROM quotes WHERE author LIKE '%' || :authorName || '%' ORDER BY savedAt DESC")
    fun getQuotesByAuthor(authorName: String): Flow<List<QuoteEntity>>

    /**
     * Searches for quotes containing specific text in content (case-insensitive)
     *
     * @param searchText The text to search for in quote content
     * @return Flow emitting list of matching QuoteEntity objects
     */
    @Query("SELECT * FROM quotes WHERE content LIKE '%' || :searchText || '%' ORDER BY savedAt DESC")
    fun searchQuotesByContent(searchText: String): Flow<List<QuoteEntity>>

    /**
     * Gets the total count of saved quotes
     *
     * @return The total number of quotes in the database
     */
    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int

    /**
     * Deletes all quotes from the database
     *
     * Use with caution - this will remove all saved quotes.
     */
    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
}