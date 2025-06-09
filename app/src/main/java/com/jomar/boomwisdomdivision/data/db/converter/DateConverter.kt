package com.jomar.boomwisdomdivision.data.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.Date

/**
 * Room type converters for complex data types
 *
 * Provides conversion methods for storing complex data types in SQLite database.
 * Handles conversion between Kotlin types and database-compatible types.
 */
class DateConverter {

    /**
     * Converts Date object to Long timestamp for database storage
     *
     * @param date The Date object to convert
     * @return Long timestamp in milliseconds, or null if date is null
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts Long timestamp to Date object
     *
     * @param timestamp The timestamp in milliseconds
     * @return Date object, or null if timestamp is null
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Converts List<String> to JSON string for database storage
     *
     * @param tags The list of strings to convert
     * @return JSON string representation, or null if tags is null
     */
    @TypeConverter
    fun fromStringList(tags: List<String>?): String? {
        if (tags == null) return null
        
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        
        return adapter.toJson(tags)
    }

    /**
     * Converts JSON string to List<String>
     *
     * @param tagsJson The JSON string to convert
     * @return List of strings, or empty list if tagsJson is null or invalid
     */
    @TypeConverter
    fun toStringList(tagsJson: String?): List<String> {
        if (tagsJson == null) return emptyList()
        
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(type)
        
        return try {
            adapter.fromJson(tagsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
