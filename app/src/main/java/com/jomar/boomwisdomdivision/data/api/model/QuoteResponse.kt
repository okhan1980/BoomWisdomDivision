package com.jomar.boomwisdomdivision.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response wrapper for paginated quote results from Quotable API
 *
 * Used for endpoints that return multiple quotes with pagination information.
 * Provides metadata about the current page, total count, and the list of quotes.
 *
 * @property count The number of quotes returned in this response
 * @property totalCount The total number of quotes available
 * @property page The current page number
 * @property totalPages The total number of pages available
 * @property lastItemIndex The index of the last item in this response
 * @property results The list of quotes in this page
 */
@JsonClass(generateAdapter = true)
data class QuoteResponse(
    @Json(name = "count") 
    val count: Int,
    
    @Json(name = "totalCount") 
    val totalCount: Int,
    
    @Json(name = "page") 
    val page: Int,
    
    @Json(name = "totalPages") 
    val totalPages: Int,
    
    @Json(name = "lastItemIndex") 
    val lastItemIndex: Int,
    
    @Json(name = "results") 
    val results: List<QuoteDto>
)