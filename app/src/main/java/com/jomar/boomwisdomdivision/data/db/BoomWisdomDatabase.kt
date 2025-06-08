package com.jomar.boomwisdomdivision.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.jomar.boomwisdomdivision.data.db.converter.DateConverter
import com.jomar.boomwisdomdivision.data.db.dao.QuoteDao
import com.jomar.boomwisdomdivision.data.db.entity.QuoteEntity

/**
 * Room database for BoomWisdom application
 *
 * Central database that manages saved quotes and provides access to DAOs.
 * Uses SQLite as the underlying database engine with Room as the abstraction layer.
 *
 * @property entities List of database entities (tables)
 * @property version Database schema version for migrations
 * @property exportSchema Whether to export database schema for version control
 */
@Database(
    entities = [QuoteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class BoomWisdomDatabase : RoomDatabase() {

    /**
     * Provides access to Quote data access object
     *
     * @return QuoteDao instance for database operations
     */
    abstract fun quoteDao(): QuoteDao

    companion object {
        /**
         * Database name constant
         */
        const val DATABASE_NAME = "boom_wisdom_database"

        /**
         * Volatile instance to ensure thread-safe singleton pattern
         */
        @Volatile
        private var INSTANCE: BoomWisdomDatabase? = null

        /**
         * Creates or returns existing database instance
         *
         * Uses double-checked locking pattern to ensure thread safety
         * while maintaining performance.
         *
         * @param context Application context for database creation
         * @return BoomWisdomDatabase instance
         */
        fun getDatabase(context: Context): BoomWisdomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoomWisdomDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                
                INSTANCE = instance
                instance
            }
        }

        /**
         * Creates database instance for testing
         *
         * Creates an in-memory database that is destroyed when the process is killed.
         * Should only be used for testing purposes.
         *
         * @param context Test context
         * @return BoomWisdomDatabase instance for testing
         */
        fun getTestDatabase(context: Context): BoomWisdomDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                BoomWisdomDatabase::class.java
            )
                .allowMainThreadQueries() // For testing only
                .build()
        }
    }
}