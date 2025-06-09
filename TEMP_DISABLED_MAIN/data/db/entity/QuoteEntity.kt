package com.jomar.boomwisdomdivision.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved quote in the local database
 *
 * Stores quote information in the local SQLite database for offline access
 * and user's saved quotes functionality.
 *
 * @property id Unique identifier for the quote (Primary Key)
 * @property content The actual quote text
 * @property author The author of the quote
 * @property length The character length of the quote content
 * @property tags JSON string representation of the tags list
 * @property savedAt Timestamp when the quote was saved locally (in milliseconds)
 */
@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey 
    val id: String,
    
    val content: String,
    
    val author: String,
    
    val length: Int,
    
    val tags: String, // JSON string representation of List<String>
    
    val savedAt: Long
)
