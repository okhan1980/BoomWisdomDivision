package com.jomar.boomwisdomdivision.data.di

import com.jomar.boomwisdomdivision.data.repository.QuoteRepositoryImpl
import com.jomar.boomwisdomdivision.domain.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * 
 * This module binds the domain repository interfaces to their concrete implementations
 * in the data layer. This follows the Dependency Inversion Principle by allowing
 * the domain layer to depend on abstractions rather than concrete implementations.
 * 
 * The module uses @Binds instead of @Provides for better performance, as it generates
 * less code and is more efficient for simple interface-to-implementation bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds the QuoteRepository interface to its concrete implementation.
     * 
     * This binding allows the domain layer (use cases) to depend on the
     * QuoteRepository interface while using the actual QuoteRepositoryImpl
     * implementation from the data layer. The implementation handles:
     * 
     * - Coordination between remote API and local database
     * - Data caching and offline support  
     * - Error handling and network fallbacks
     * - Data transformation between layers
     * 
     * The binding is scoped as Singleton to ensure:
     * - Single source of truth for quote data
     * - Efficient resource usage
     * - Consistent caching behavior
     * - Proper coordination between data sources
     * 
     * @param quoteRepositoryImpl The concrete implementation to bind
     * @return [QuoteRepository] interface that domain layer can depend on
     */
    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        quoteRepositoryImpl: QuoteRepositoryImpl
    ): QuoteRepository
    
    // Future repository bindings can be added here as the app grows
    // For example:
    // 
    // @Binds
    // @Singleton
    // abstract fun bindUserRepository(
    //     userRepositoryImpl: UserRepositoryImpl
    // ): UserRepository
    //
    // @Binds
    // @Singleton
    // abstract fun bindSettingsRepository(
    //     settingsRepositoryImpl: SettingsRepositoryImpl
    // ): SettingsRepository
}
