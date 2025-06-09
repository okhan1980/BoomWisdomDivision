package com.jomar.boomwisdomdivision.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for providing coroutine dispatchers.
 * 
 * This module provides properly configured coroutine dispatchers for different types
 * of operations. Using dependency injection for dispatchers allows for:
 * - Easy testing by injecting test dispatchers
 * - Centralized dispatcher configuration
 * - Better separation of concerns
 * - Performance optimization based on operation type
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    /**
     * Provides the IO dispatcher for I/O-intensive operations.
     * 
     * This dispatcher is optimized for I/O operations such as:
     * - Network requests (API calls)
     * - Database operations (Room queries)
     * - File system operations
     * - Any blocking I/O that shouldn't block the main thread
     * 
     * The IO dispatcher uses a shared pool of threads that can grow as needed
     * to handle concurrent I/O operations efficiently.
     * 
     * @return [CoroutineDispatcher] optimized for I/O operations
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    /**
     * Provides the Default dispatcher for CPU-intensive operations.
     * 
     * This dispatcher is optimized for CPU-intensive work such as:
     * - Data processing and transformations
     * - Complex calculations
     * - Sorting and filtering large datasets
     * - JSON parsing (when not using streaming)
     * 
     * The Default dispatcher is limited to the number of CPU cores
     * to prevent overwhelming the system with compute tasks.
     * 
     * @return [CoroutineDispatcher] optimized for CPU-intensive operations
     */
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    /**
     * Provides the Main dispatcher for UI operations.
     * 
     * This dispatcher runs on the main/UI thread and is used for:
     * - UI updates and state changes
     * - View model state emissions
     * - Quick, non-blocking operations
     * - Coordinating with the Android UI lifecycle
     * 
     * Operations on this dispatcher should be fast to avoid blocking the UI.
     * Heavy work should be delegated to IO or Default dispatchers.
     * 
     * @return [CoroutineDispatcher] that runs on the main thread
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    
    /**
     * Provides the Main.immediate dispatcher for immediate UI operations.
     * 
     * This dispatcher attempts to execute on the main thread immediately
     * if already on the main thread, avoiding unnecessary dispatching overhead.
     * Used for:
     * - Performance-critical UI updates
     * - Operations that are already on the main thread
     * - Reducing dispatcher overhead in hot paths
     * 
     * @return [CoroutineDispatcher] for immediate main thread execution
     */
    @Provides
    @Singleton
    @MainImmediateDispatcher
    fun provideMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
    
    /**
     * Provides an unconfined dispatcher for testing and special cases.
     * 
     * This dispatcher starts coroutines in the caller thread but only until
     * the first suspension point. After suspension, it resumes in whatever
     * thread the suspending function chooses.
     * 
     * Mainly used for:
     * - Unit testing (though TestDispatcher is often preferred)
     * - Special cases where thread affinity doesn't matter
     * - Performance testing scenarios
     * 
     * Note: Generally not recommended for production use due to unpredictable
     * thread behavior after suspension.
     * 
     * @return [CoroutineDispatcher] with unconfined thread behavior
     */
    @Provides
    @Singleton
    @UnconfinedDispatcher
    fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Qualifier annotation for the IO dispatcher.
 * 
 * Use this annotation to inject the IO dispatcher for I/O-intensive operations
 * like network requests and database operations.
 * 
 * Example usage:
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Qualifier annotation for the Default dispatcher.
 * 
 * Use this annotation to inject the Default dispatcher for CPU-intensive operations
 * like data processing and complex calculations.
 * 
 * Example usage:
 * ```kotlin
 * class DataProcessor @Inject constructor(
 *     @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Qualifier annotation for the Main dispatcher.
 * 
 * Use this annotation to inject the Main dispatcher for UI operations
 * and main thread work.
 * 
 * Example usage:
 * ```kotlin
 * class MyViewModel @Inject constructor(
 *     @MainDispatcher private val mainDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Qualifier annotation for the Main.immediate dispatcher.
 * 
 * Use this annotation to inject the Main.immediate dispatcher for
 * performance-critical UI operations.
 * 
 * Example usage:
 * ```kotlin
 * class UiController @Inject constructor(
 *     @MainImmediateDispatcher private val mainImmediateDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainImmediateDispatcher

/**
 * Qualifier annotation for the Unconfined dispatcher.
 * 
 * Use this annotation to inject the Unconfined dispatcher for testing
 * or special cases where thread affinity doesn't matter.
 * 
 * Example usage:
 * ```kotlin
 * class TestHelper @Inject constructor(
 *     @UnconfinedDispatcher private val unconfinedDispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnconfinedDispatcher
