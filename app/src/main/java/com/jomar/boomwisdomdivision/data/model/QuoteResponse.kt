package com.jomar.boomwisdomdivision.data.model

import com.squareup.moshi.Json

/**
 * Data model for ZenQuotes API response
 * Matches the JSON structure from https://zenquotes.io/api/random
 * Returns array with single quote object: [{"q": "quote", "a": "author", "h": "html"}]
 */
data class QuoteResponse(
    @Json(name = "q")
    val content: String,
    
    @Json(name = "a")
    val author: String,
    
    @Json(name = "h")
    val html: String = "",
    
    // Generate a simple ID for compatibility
    val id: String = java.util.UUID.randomUUID().toString()
)