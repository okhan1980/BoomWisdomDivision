package com.jomar.boomwisdomdivision.domain.model

/**
 * Domain model representing a motivational quote.
 * 
 * This is the core domain entity that represents quotes throughout the application.
 * It maintains clean architecture principles by having no dependencies on external frameworks
 * or data layer implementations.
 *
 * @property id Unique identifier for the quote
 * @property content The text content of the quote
 * @property author The author or source of the quote
 * @property length Character length of the quote content
 * @property tags List of tags associated with the quote for categorization
 * @property savedAt Timestamp when the quote was saved to favorites (null if not saved)
 */
data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val length: Int,
    val tags: List<String>,
    val savedAt: Long? = null
) {
    /**
     * Determines if this quote is currently saved to favorites.
     * 
     * @return true if the quote has been saved to favorites, false otherwise
     */
    val isSaved: Boolean
        get() = savedAt != null

    /**
     * Creates a copy of this quote marked as saved with the current timestamp.
     * 
     * @param timestamp The timestamp when the quote was saved (defaults to current time)
     * @return A new Quote instance with the savedAt timestamp set
     */
    fun markAsSaved(timestamp: Long = System.currentTimeMillis()): Quote {
        return copy(savedAt = timestamp)
    }

    /**
     * Creates a copy of this quote marked as not saved (removes savedAt timestamp).
     * 
     * @return A new Quote instance with savedAt set to null
     */
    fun markAsUnsaved(): Quote {
        return copy(savedAt = null)
    }

    /**
     * Returns a formatted string suitable for display purposes.
     * 
     * @return Formatted quote string with content and author attribution
     */
    fun getDisplayText(): String {
        return if (author.isNotBlank()) {
            "$content\n\n- $author"
        } else {
            content
        }
    }
}
