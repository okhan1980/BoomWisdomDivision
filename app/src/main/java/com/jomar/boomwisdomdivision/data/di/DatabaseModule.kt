package com.jomar.boomwisdomdivision.data.di

import android.content.Context
import androidx.room.Room
import com.jomar.boomwisdomdivision.data.db.BoomWisdomDatabase
import com.jomar.boomwisdomdivision.data.db.dao.QuoteDao
import com.jomar.boomwisdomdivision.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 * 
 * This module configures the local database layer for the Boom Wisdom Division app,
 * including Room database setup and DAO provisioning. All database dependencies
 * are scoped as singletons to ensure proper database lifecycle management.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the main Room database instance for the application.
     * 
     * The database is configured with:
     * - Application context for proper lifecycle management
     * - Database name and version from constants
     * - Fallback to destructive migration for development (should be changed for production)
     * - Build-specific optimizations
     * 
     * @param context Application context provided by Hilt
     * @return Configured [BoomWisdomDatabase] instance
     */
    @Provides
    @Singleton
    fun provideBoomWisdomDatabase(
        @ApplicationContext context: Context
    ): BoomWisdomDatabase {
        return Room.databaseBuilder(
            context,
            BoomWisdomDatabase::class.java,
            Constants.DATABASE_NAME
        ).apply {
            // Database configuration based on build variant
            if (com.jomar.boomwisdomdivision.BuildConfig.DEBUG) {
                // Development configuration
                fallbackToDestructiveMigration() // WARNING: This destroys data on schema changes
                // In production, you should use proper migration strategies instead
            } else {
                // Production configuration
                // Add proper migration strategies here when releasing to production
                // fallbackToDestructiveMigrationOnDowngrade() // Only for downgrades
            }
            
            // Optional: Enable query logging in debug builds
            if (com.jomar.boomwisdomdivision.BuildConfig.DEBUG) {
                setQueryCallback(
                    object : androidx.room.RoomDatabase.QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            // Log database queries in debug builds
                            println("Database Query: $sqlQuery")
                            if (bindArgs.isNotEmpty()) {
                                println("Query Args: $bindArgs")
                            }
                        }
                    },
                    java.util.concurrent.Executors.newSingleThreadExecutor()
                )
            }
        }.build()
    }
    
    /**
     * Provides the QuoteDao for quote-related database operations.
     * 
     * The DAO provides type-safe access to the quotes table with methods for
     * inserting, updating, querying, and deleting quote records. All operations
     * return appropriate types (Flow for reactive queries, suspend functions for mutations).
     * 
     * @param database The main database instance
     * @return [QuoteDao] for quote database operations
     */
    @Provides
    @Singleton
    fun provideQuoteDao(database: BoomWisdomDatabase): QuoteDao {
        return database.quoteDao()
    }
}