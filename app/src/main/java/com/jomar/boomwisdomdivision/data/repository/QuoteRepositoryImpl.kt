package com.jomar.boomwisdomdivision.data.repository

import com.jomar.boomwisdomdivision.data.api.QuotableApi
import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.db.dao.QuoteDao
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity
import com.jomar.boomwisdomdivision.data.db.converter.DateConverter
import com.jomar.boomwisdomdivision.data.mapper.toDomain
import com.jomar.boomwisdomdivision.data.mapper.toEntity
import com.jomar.boomwisdomdivision.domain.model.Quote
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import com.jomar.boomwisdomdivision.core.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of QuoteRepository following Repository pattern
 *
 * Manages data operations for quotes, coordinating between remote API and local database.
 * Provides a single source of truth for quote data throughout the application.
 *
 * This implementation will be bound to the domain repository interface by Agent 3's DI modules.
 *
 * @property quotableApi Remote API service for fetching quotes
 * @property quoteDao Local database access object for cached/saved quotes
 * @property dateConverter Utility for converting between data types
 */
@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quotableApi: QuotableApi,
    private val quoteDao: QuoteDao,
    private val dateConverter: DateConverter
) : QuoteRepository {

    /**
     * Fetches a random quote from the remote API (Domain interface implementation)
     */
    override suspend fun getRandomQuote(): Result<Quote> {
        return try {
            val response = quotableApi.getRandomQuote()
            when {
                response.isSuccessful -> {
                    val quoteDto = response.body()
                    if (quoteDto != null) {
                        Result.Success(quoteDto.toDomain())
                    } else {
                        Result.Error(Exception("Empty response from API"))
                    }
                }
                else -> {
                    Result.Error(Exception("API Error: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching random quote")
            Result.Error(e)
        }
    }

    /**
     * Fetches a random quote from the remote API with filters (Data layer method)
     *
     * @param tags Optional comma-separated list of tag names to filter quotes
     * @param author Optional author name to filter quotes
     * @param minLength Optional minimum length of the quote content
     * @param maxLength Optional maximum length of the quote content
     * @return Result containing QuoteDto or error information
     */
    suspend fun getRandomQuoteWithFilters(
        tags: String? = null,
        author: String? = null,
        minLength: Int? = null,
        maxLength: Int? = null
    ): ApiResult<QuoteDto> {
        return try {
            val response = quotableApi.getRandomQuote(tags, author, minLength, maxLength)
            handleApiResponse(response)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching random quote")
            ApiResult.Error(ApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    /**
     * Searches for quotes from the remote API
     *
     * @param query Search query text
     * @param page Page number for pagination
     * @param limit Number of results per page
     * @return Result containing list of QuoteDto or error information
     */
    suspend fun searchQuotes(
        query: String,
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<List<QuoteDto>> {
        return try {
            val response = quotableApi.searchQuotes(query, page, limit)
            when {
                response.isSuccessful -> {
                    val quoteResponse = response.body()
                    if (quoteResponse != null) {
                        ApiResult.Success(quoteResponse.results)
                    } else {
                        ApiResult.Error(ApiError.EmptyResponse)
                    }
                }
                else -> {
                    ApiResult.Error(ApiError.HttpError(response.code(), response.message()))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching quotes")
            ApiResult.Error(ApiError.NetworkError(e.message ?: "Unknown network error"))
        }
    }

    /**
     * Saves a quote to the local database (Domain interface implementation)
     */
    override suspend fun saveQuote(quote: Quote): Result<Quote> {
        return try {
            val quoteEntity = quote.toEntity()
            quoteDao.insertQuote(quoteEntity)
            Timber.d("Quote saved successfully: ${quote.id}")
            Result.Success(quote.markAsSaved())
        } catch (e: Exception) {
            Timber.e(e, "Error saving quote: ${quote.id}")
            Result.Error(e)
        }
    }

    /**
     * Saves a quote to the local database (Data layer method)
     *
     * @param quoteDto The quote data to save
     * @return Result indicating success or failure
     */
    suspend fun saveQuoteDto(quoteDto: QuoteDto): DatabaseResult<Unit> {
        return try {
            val quoteEntity = mapQuoteDtoToEntity(quoteDto, System.currentTimeMillis())
            quoteDao.insertQuote(quoteEntity)
            Timber.d("Quote saved successfully: ${quoteDto.id}")
            DatabaseResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error saving quote: ${quoteDto.id}")
            DatabaseResult.Error(DatabaseError.InsertError(e.message ?: "Failed to save quote"))
        }
    }

    /**
     * Deletes a quote from the local database (Domain interface implementation)
     */
    override suspend fun deleteQuote(quoteId: String): Result<Unit> {
        return try {
            val deletedRows = quoteDao.deleteQuoteById(quoteId)
            if (deletedRows > 0) {
                Timber.d("Quote deleted successfully: $quoteId")
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Quote not found: $quoteId"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting quote: $quoteId")
            Result.Error(e)
        }
    }

    /**
     * Checks if a quote is saved in the local database (Domain interface implementation)
     */
    override suspend fun isQuoteSaved(quoteId: String): Result<Boolean> {
        return try {
            val isSaved = quoteDao.isQuoteSaved(quoteId)
            Result.Success(isSaved)
        } catch (e: Exception) {
            Timber.e(e, "Error checking if quote is saved: $quoteId")
            Result.Error(e)
        }
    }

    /**
     * Retrieves all saved quotes from the local database (Domain interface implementation)
     */
    override fun getSavedQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Retrieves a specific quote by its ID (Domain interface implementation)
     */
    override suspend fun getQuoteById(quoteId: String): Result<Quote> {
        return try {
            val entity = quoteDao.getQuoteById(quoteId)
            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                Result.Error(Exception("Quote not found: $quoteId"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving quote by ID: $quoteId")
            Result.Error(e)
        }
    }

    /**
     * Retrieves all saved quotes from the local database as DTOs (Data layer method)
     *
     * @return Flow emitting list of saved quotes as DTOs
     */
    fun getSavedQuotesAsDto(): Flow<List<QuoteDto>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { mapQuoteEntityToDto(it) }
        }
    }

    /**
     * Searches saved quotes by content
     *
     * @param searchText Text to search for in quote content
     * @return Flow emitting list of matching quotes as DTOs
     */
    fun searchSavedQuotes(searchText: String): Flow<List<QuoteDto>> {
        return quoteDao.searchQuotesByContent(searchText).map { entities ->
            entities.map { mapQuoteEntityToDto(it) }
        }
    }

    /**
     * Gets quotes by specific author from saved quotes
     *
     * @param authorName Author name to filter by
     * @return Flow emitting list of quotes by the author as DTOs
     */
    fun getQuotesByAuthor(authorName: String): Flow<List<QuoteDto>> {
        return quoteDao.getQuotesByAuthor(authorName).map { entities ->
            entities.map { mapQuoteEntityToDto(it) }
        }
    }

    /**
     * Gets the total count of saved quotes
     *
     * @return Number of saved quotes
     */
    suspend fun getSavedQuoteCount(): Int {
        return try {
            quoteDao.getQuoteCount()
        } catch (e: Exception) {
            Timber.e(e, "Error getting saved quote count")
            0
        }
    }

    /**
     * Handles API response and converts to Result type
     *
     * @param response Retrofit response object
     * @return ApiResult with success data or error information
     */
    private fun <T> handleApiResponse(response: Response<T>): ApiResult<T> {
        return when {
            response.isSuccessful -> {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(ApiError.EmptyResponse)
                }
            }
            response.code() == 404 -> {
                ApiResult.Error(ApiError.NotFound)
            }
            response.code() in 400..499 -> {
                ApiResult.Error(ApiError.ClientError(response.code(), response.message()))
            }
            response.code() in 500..599 -> {
                ApiResult.Error(ApiError.ServerError(response.code(), response.message()))
            }
            else -> {
                ApiResult.Error(ApiError.HttpError(response.code(), response.message()))
            }
        }
    }

    /**
     * Maps QuoteDto to QuoteEntity for database storage
     *
     * @param dto Quote data transfer object from API
     * @param savedAt Timestamp when the quote was saved
     * @return QuoteEntity for database storage
     */
    private fun mapQuoteDtoToEntity(dto: QuoteDto, savedAt: Long): QuoteEntity {
        return QuoteEntity(
            id = dto.id,
            content = dto.content,
            author = dto.author,
            length = dto.length,
            tags = dateConverter.fromStringList(dto.tags) ?: "[]",
            savedAt = savedAt
        )
    }

    /**
     * Maps QuoteEntity to QuoteDto for presentation layer
     *
     * @param entity Quote entity from database
     * @return QuoteDto for use in other layers
     */
    private fun mapQuoteEntityToDto(entity: QuoteEntity): QuoteDto {
        return QuoteDto(
            id = entity.id,
            content = entity.content,
            author = entity.author,
            length = entity.length,
            tags = dateConverter.toStringList(entity.tags)
        )
    }
}

/**
 * Sealed class representing API operation results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: ApiError) : ApiResult<Nothing>()
}

/**
 * Sealed class representing database operation results
 */
sealed class DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>()
    data class Error(val error: DatabaseError) : DatabaseResult<Nothing>()
}

/**
 * Sealed class representing API errors
 */
sealed class ApiError {
    object EmptyResponse : ApiError()
    object NotFound : ApiError()
    data class NetworkError(val message: String) : ApiError()
    data class HttpError(val code: Int, val message: String) : ApiError()
    data class ClientError(val code: Int, val message: String) : ApiError()
    data class ServerError(val code: Int, val message: String) : ApiError()
}

/**
 * Sealed class representing database errors
 */
sealed class DatabaseError {
    data class InsertError(val message: String) : DatabaseError()
    data class DeleteError(val message: String) : DatabaseError()
    data class NotFound(val message: String) : DatabaseError()
    data class QueryError(val message: String) : DatabaseError()
}