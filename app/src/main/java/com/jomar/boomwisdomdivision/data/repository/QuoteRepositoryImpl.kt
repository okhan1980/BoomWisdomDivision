package com.jomar.boomwisdomdivision.data.repository

import com.jomar.boomwisdomdivision.data.api.QuotableApi
import com.jomar.boomwisdomdivision.data.model.QuoteResponse
import com.jomar.boomwisdomdivision.model.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple quote repository implementation without DI framework
 * Manages quote caching and API calls for Phase 3
 */
class QuoteRepositoryImpl {
    
    private val quotableApi = QuotableApi()
    private val cacheMutex = Mutex()
    
    // In-memory cache for quotes
    private val _cachedQuotes = MutableStateFlow<List<Quote>>(emptyList())
    val cachedQuotes: StateFlow<List<Quote>> = _cachedQuotes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Fallback quotes for offline use (from Phase 2)
    private val fallbackQuotes = listOf(
        Quote(
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Innovation distinguishes between a leader and a follower.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Life is what happens to you while you're busy making other plans.",
            author = "John Lennon"
        ),
        Quote(
            text = "The future belongs to those who believe in the beauty of their dreams.",
            author = "Eleanor Roosevelt"
        ),
        Quote(
            text = "It is during our darkest moments that we must focus to see the light.",
            author = "Aristotle"
        ),
        Quote(
            text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            author = "Winston Churchill"
        ),
        Quote(
            text = "The way to get started is to quit talking and begin doing.",
            author = "Walt Disney"
        ),
        Quote(
            text = "Don't let yesterday take up too much of today.",
            author = "Will Rogers"
        ),
        Quote(
            text = "You learn more from failure than from success. Don't let it stop you.",
            author = "Unknown"
        ),
        Quote(
            text = "If you are working on something that you really care about, you don't have to be pushed.",
            author = "Steve Jobs"
        ),
        Quote(
            text = "Whether you think you can or you think you can't, you're right.",
            author = "Henry Ford"
        ),
        Quote(
            text = "The only impossible journey is the one you never begin.",
            author = "Tony Robbins"
        )
    )
    
    companion object {
        private const val CACHE_TARGET_SIZE = 20  // Increased cache size
        private const val REFILL_THRESHOLD = 10   // Higher threshold for refill
        
        // Singleton instance for simplified architecture
        @Volatile
        private var INSTANCE: QuoteRepositoryImpl? = null
        
        fun getInstance(): QuoteRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuoteRepositoryImpl().also { INSTANCE = it }
            }
        }
    }
    
    init {
        // Initialize with fallback quotes
        _cachedQuotes.value = fallbackQuotes
    }
    
    /**
     * Get a random quote - tries API first, then falls back to cache
     */
    suspend fun getRandomQuote(): Quote {
        // Try to get a fresh quote from API first (with some probability)
        return try {
            val shouldFetchFresh = kotlin.random.Random.nextDouble() < 0.7 // 70% chance to fetch fresh
            
            if (shouldFetchFresh) {
                println("Attempting to fetch fresh quote from API...") // Debug
                val result = quotableApi.getRandomQuote()
                if (result.isSuccess) {
                    val apiQuote = result.getOrNull()?.toQuote()
                    if (apiQuote != null) {
                        println("Successfully got fresh quote: ${apiQuote.text.take(50)}...") // Debug
                        // Add to cache for future use
                        cacheMutex.withLock {
                            val currentQuotes = _cachedQuotes.value.toMutableList()
                            currentQuotes.add(apiQuote)
                            _cachedQuotes.value = currentQuotes.distinctBy { it.text }
                        }
                        return apiQuote
                    }
                }
            }
            
            // Fallback to cached quotes
            val currentCache = _cachedQuotes.value
            println("Using cached quote (cache size: ${currentCache.size})") // Debug
            
            // If cache is low, try to refill in background (note: simplified for now)
            if (currentCache.size < REFILL_THRESHOLD) {
                // Note: Background refill will happen on next refresh call
                println("Cache is low (${currentCache.size} quotes), will refill on next refresh")
            }
            
            // Return random quote from current cache
            if (currentCache.isNotEmpty()) {
                currentCache.random()
            } else {
                // Ultimate fallback to hardcoded quotes
                println("Using fallback hardcoded quote") // Debug
                fallbackQuotes.random()
            }
        } catch (e: Exception) {
            println("Error in getRandomQuote: ${e.message}") // Debug
            // Fallback to cached or hardcoded quotes
            val currentCache = _cachedQuotes.value
            if (currentCache.isNotEmpty()) {
                currentCache.random()
            } else {
                fallbackQuotes.random()
            }
        }
    }
    
    /**
     * Get all cached quotes
     */
    fun getAllQuotes(): List<Quote> {
        return _cachedQuotes.value
    }
    
    /**
     * Refresh quote cache from API
     */
    suspend fun refreshQuotes() {
        _isLoading.value = true
        _error.value = null
        
        try {
            println("Starting quote refresh...") // Debug logging
            // Start with smaller batch to respect rate limits, then gradually increase cache
            val result = quotableApi.getMultipleRandomQuotes(10) // Reduced initial batch
            if (result.isSuccess) {
                val quotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
                println("Successfully fetched ${quotes.size} quotes from API") // Debug logging
                cacheMutex.withLock {
                    // Combine new quotes with existing fallback quotes for better variety
                    val combinedQuotes = (fallbackQuotes + quotes).distinctBy { it.text }
                    _cachedQuotes.value = combinedQuotes
                    println("Cache now contains ${combinedQuotes.size} total quotes") // Debug logging
                }
                _error.value = null // Clear any previous errors
            } else {
                val exception = result.exceptionOrNull()
                val errorMsg = "Network error: ${exception?.message ?: "Unable to fetch quotes"}"
                println("Quote refresh failed: $errorMsg") // Debug logging
                _error.value = errorMsg
                // Keep existing cache on failure
            }
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message ?: "Unknown error occurred"}"
            println("Quote refresh exception: $errorMsg") // Debug logging
            _error.value = errorMsg
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Refill cache when running low
     */
    private suspend fun refillCache() {
        if (_isLoading.value) return // Already loading
        
        _isLoading.value = true
        
        try {
            // Fetch fewer quotes for refill to respect ZenQuotes rate limits (100/day)
            val quotesToFetch = minOf(5, CACHE_TARGET_SIZE - _cachedQuotes.value.size)
            val result = quotableApi.getMultipleRandomQuotes(quotesToFetch)
            if (result.isSuccess) {
                val newQuotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
                println("Refill cache: fetched ${newQuotes.size} new quotes") // Debug logging
                cacheMutex.withLock {
                    val currentQuotes = _cachedQuotes.value.toMutableList()
                    currentQuotes.addAll(newQuotes)
                    // Remove duplicates and keep only unique quotes
                    _cachedQuotes.value = currentQuotes.distinctBy { it.text }
                }
            }
            // Silently handle errors during background refill
            // User still has cached quotes to use
        } catch (e: Exception) {
            println("Background refill failed: ${e.message}") // Debug logging
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Extension function to convert QuoteResponse to Quote
 */
private fun QuoteResponse.toQuote(): Quote {
    return Quote(
        text = this.content,
        author = this.author,
        id = this.id
    )
}