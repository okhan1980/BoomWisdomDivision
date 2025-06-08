package com.jomar.boomwisdomdivision.data.mapper

import com.jomar.boomwisdomdivision.data.api.model.QuoteDto
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity
import com.jomar.boomwisdomdivision.domain.model.Quote

/**
 * Mapper functions for converting between data layer and domain layer models.
 * 
 * These extension functions provide clean, type-safe mapping between:
 * - DTOs from the API layer
 * - Entities from the database layer  
 * - Domain models used by the business logic
 * 
 * This follows Clean Architecture principles by keeping the domain layer
 * independent of data layer implementations.
 */

/**
 * Converts a QuoteDto from the API to a domain Quote model.
 * 
 * @return Domain Quote model
 */
fun QuoteDto.toDomain(): Quote {
    return Quote(
        id = id,
        content = content,
        author = author,
        length = length,
        tags = tags,
        savedAt = null // API quotes are not saved by default
    )
}

/**
 * Converts a QuoteEntity from the database to a domain Quote model.
 * 
 * @return Domain Quote model with savedAt timestamp
 */
fun QuoteEntity.toDomain(): Quote {
    return Quote(
        id = id,
        content = content,
        author = author,
        length = length,
        tags = parseTagsFromJson(tags),
        savedAt = savedAt
    )
}

/**
 * Converts a domain Quote to a QuoteEntity for database storage.
 * 
 * @return Database entity ready for storage
 */
fun Quote.toEntity(): QuoteEntity {
    return QuoteEntity(
        id = id,
        content = content,
        author = author,
        length = length,
        tags = formatTagsToJson(tags),
        savedAt = savedAt ?: System.currentTimeMillis()
    )
}

/**
 * Converts a domain Quote to a QuoteDto.
 * Useful for caching API responses or testing.
 * 
 * @return DTO representation of the quote
 */
fun Quote.toDto(): QuoteDto {
    return QuoteDto(
        id = id,
        content = content,
        author = author,
        length = length,
        tags = tags
    )
}

/**
 * Parses tags from JSON string stored in database.
 * 
 * @param tagsJson JSON string representation of tags
 * @return List of tags, empty list if parsing fails
 */
private fun parseTagsFromJson(tagsJson: String): List<String> {
    return try {
        // Simple JSON parsing for string arrays
        if (tagsJson.startsWith("[") && tagsJson.endsWith("]")) {
            tagsJson.substring(1, tagsJson.length - 1)
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Formats tags list to JSON string for database storage.
 * 
 * @param tags List of tag strings
 * @return JSON string representation
 */
private fun formatTagsToJson(tags: List<String>): String {
    return tags.joinToString(
        prefix = "[",
        postfix = "]",
        separator = ","
    ) { "\"$it\"" }
}