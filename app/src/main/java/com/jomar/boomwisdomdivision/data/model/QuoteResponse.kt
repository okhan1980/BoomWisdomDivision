package com.jomar.boomwisdomdivision.data.model

import com.squareup.moshi.Json

/**
 * Data model for Quotable API response
 * Matches the JSON structure from https://api.quotable.io/quotes/random
 */
data class QuoteResponse(
    @Json(name = "_id")
    val id: String,
    
    @Json(name = "content")
    val content: String,
    
    @Json(name = "author")
    val author: String,
    
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    
    @Json(name = "authorSlug")
    val authorSlug: String = "",
    
    @Json(name = "length")
    val length: Int = 0,
    
    @Json(name = "dateAdded")
    val dateAdded: String = "",
    
    @Json(name = "dateModified")
    val dateModified: String = ""
)