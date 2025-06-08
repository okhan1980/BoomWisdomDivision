package com.jomar.boomwisdomdivision.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for Quote from Quotable API
 *
 * Maps the JSON response from the Quotable API to a Kotlin data class.
 * Used to represent quote data received from the API endpoint.
 *
 * @property id Unique identifier for the quote (mapped from "_id" field)
 * @property content The actual quote text
 * @property author The author of the quote
 * @property length The character length of the quote content
 * @property tags List of tags associated with the quote
 */
@JsonClass(generateAdapter = true)
data class QuoteDto(
    @Json(name = "_id") 
    val id: String,
    
    @Json(name = "content") 
    val content: String,
    
    @Json(name = "author") 
    val author: String,
    
    @Json(name = "length") 
    val length: Int,
    
    @Json(name = "tags") 
    val tags: List<String>
)