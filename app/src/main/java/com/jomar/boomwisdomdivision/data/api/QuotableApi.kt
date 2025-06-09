package com.jomar.boomwisdomdivision.data.api

import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.api.model.QuoteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API interface for Quotable service
 *
 * Defines endpoints for interacting with the Quotable API (https://api.quotable.io).
 * Provides methods for fetching random quotes, searching quotes, and retrieving specific quotes.
 *
 * Base URL: https://api.quotable.io
 */
interface QuotableApi {

    /**
     * Fetches a random quote from the API
     *
     * @param tags Optional comma-separated list of tag names to filter quotes
     * @param author Optional author name to filter quotes
     * @param minLength Optional minimum length of the quote content
     * @param maxLength Optional maximum length of the quote content
     * @return Response containing a single QuoteDto
     */
    @GET("random")
    suspend fun getRandomQuote(
        @Query("tags") tags: String? = null,
        @Query("author") author: String? = null,
        @Query("minLength") minLength: Int? = null,
        @Query("maxLength") maxLength: Int? = null
    ): Response<QuoteDto>

    /**
     * Fetches a paginated list of quotes
     *
     * @param page Page number (default: 1)
     * @param limit Number of quotes per page (default: 20, max: 150)
     * @param tags Optional comma-separated list of tag names to filter quotes
     * @param author Optional author name to filter quotes
     * @param sortBy Optional field to sort by (dateAdded, dateModified, author, content)
     * @param order Optional sort order (asc, desc)
     * @return Response containing QuoteResponse with paginated results
     */
    @GET("quotes")
    suspend fun getQuotes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null,
        @Query("author") author: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("order") order: String? = null
    ): Response<QuoteResponse>

    /**
     * Fetches a specific quote by its ID
     *
     * @param id The unique identifier of the quote
     * @return Response containing a single QuoteDto
     */
    @GET("quotes/{id}")
    suspend fun getQuoteById(
        @Path("id") id: String
    ): Response<QuoteDto>

    /**
     * Searches for quotes containing the specified text
     *
     * @param query The search query text
     * @param page Page number (default: 1)
     * @param limit Number of quotes per page (default: 20, max: 150)
     * @param fields Optional comma-separated list of fields to search (content, author, tags)
     * @return Response containing QuoteResponse with matching results
     */
    @GET("search/quotes")
    suspend fun searchQuotes(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String? = null
    ): Response<QuoteResponse>
}
