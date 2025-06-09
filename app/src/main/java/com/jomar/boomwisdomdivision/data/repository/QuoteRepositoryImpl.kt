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
        private const val CACHE_TARGET_SIZE = 15
        private const val REFILL_THRESHOLD = 5
        
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
     * Get a random quote from cache, falling back to API if needed
     */
    suspend fun getRandomQuote(): Quote {
        val currentCache = _cachedQuotes.value
        
        // If cache is low, try to refill in background
        if (currentCache.size < REFILL_THRESHOLD) {
            refillCache()
        }
        
        // Return random quote from current cache
        return if (currentCache.isNotEmpty()) {
            currentCache.random()
        } else {
            // Fallback to hardcoded quotes if everything fails
            fallbackQuotes.random()
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
            val result = quotableApi.getMultipleRandomQuotes(CACHE_TARGET_SIZE)
            if (result.isSuccess) {
                val quotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
                cacheMutex.withLock {
                    _cachedQuotes.value = quotes
                }
            } else {
                val exception = result.exceptionOrNull()
                _error.value = exception?.message ?: "Failed to fetch quotes"
                // Keep existing cache on failure
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error occurred"
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
            val result = quotableApi.getMultipleRandomQuotes(CACHE_TARGET_SIZE - _cachedQuotes.value.size)
            if (result.isSuccess) {
                val newQuotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
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
            // Silently handle errors during background refill
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