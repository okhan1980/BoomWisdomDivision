package com.jomar.boomwisdomdivision.data.model

import com.squareup.moshi.Json

/**
 * Data model for DummyJSON Quotes API response
 * Matches the JSON structure from https://dummyjson.com/quotes/random
 * Returns direct quote object: {"id": 1, "quote": "...", "author": "..."}
 */
data class QuoteResponse(
    @Json(name = "id")
    val id: Int,
    
    @Json(name = "quote")
    val content: String,
    
    @Json(name = "author")
    val author: String
)