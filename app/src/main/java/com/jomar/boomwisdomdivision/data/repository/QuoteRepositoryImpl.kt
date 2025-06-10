package com.jomar.boomwisdomdivision.data.repository

import com.jomar.boomwisdomdivision.data.api.QuotableApi
import com.jomar.boomwisdomdivision.data.model.QuoteResponse
import com.jomar.boomwisdomdivision.model.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    
    // Track displayed quotes to prevent repetition
    private val displayedQuoteIds = mutableSetOf<Int>()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Emergency fallback quote for when API is completely unavailable
    private val emergencyQuote = Quote(
        text = "Unable to fetch quotes. Please check your internet connection.",
        author = "System"
    )
    
    companion object {
        private const val CACHE_TARGET_SIZE = 50  // Much larger cache since Quotable.io has good limits
        private const val REFILL_THRESHOLD = 20   // Higher threshold for refill
        
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
        // Start with empty cache - force API usage
        _cachedQuotes.value = emptyList()
    }
    
    /**
     * Get a random quote - prioritizes fresh API quotes with Quotable.io's generous rate limits
     */
    suspend fun getRandomQuote(): Quote {
        println("=== QuoteRepositoryImpl.getRandomQuote() called ===")
        println("📊 Displayed quotes so far: ${displayedQuoteIds.size}")
        
        // Try API first since Quotable.io has generous rate limits (180/minute)
        try {
            val result = quotableApi.getRandomQuote()
            
            if (result.isSuccess) {
                val apiQuote = result.getOrNull()?.toQuote()
                if (apiQuote != null) {
                    // Check if we've already displayed this specific quote
                    if (!displayedQuoteIds.contains(apiQuote.id.toInt())) {
                        println("✅ API Success! Got NEW quote: \"${apiQuote.text.take(50)}...\" - ${apiQuote.author}")
                        
                        // Mark as displayed
                        displayedQuoteIds.add(apiQuote.id.toInt())
                        
                        // Add to cache for offline use
                        cacheMutex.withLock {
                            val currentQuotes = _cachedQuotes.value.toMutableList()
                            // Only add if not already in cache
                            if (currentQuotes.none { it.id == apiQuote.id }) {
                                currentQuotes.add(apiQuote)
                                _cachedQuotes.value = currentQuotes
                                println("📦 Added to cache. Cache size now: ${_cachedQuotes.value.size}")
                            }
                        }
                        
                        return apiQuote
                    } else {
                        println("⚠️ Quote already displayed (ID: ${apiQuote.id}), trying cache...")
                    }
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: ""
                println("❌ API call failed: $error")
            }
        } catch (e: Exception) {
            println("❌ Exception in API call: ${e.message}")
        }
        
        // Fallback to cache if API fails or returns duplicate
        val currentCache = _cachedQuotes.value
        val undisplayedQuotes = currentCache.filter { !displayedQuoteIds.contains(it.id.toInt()) }
        println("📦 Falling back to cache. Undisplayed quotes available: ${undisplayedQuotes.size}")
        
        if (undisplayedQuotes.isNotEmpty()) {
            val cachedQuote = undisplayedQuotes.random()
            displayedQuoteIds.add(cachedQuote.id.toInt())
            println("📦 Using cached quote: \"${cachedQuote.text.take(50)}...\" - ${cachedQuote.author}")
            return cachedQuote
        }
        
        // All quotes have been displayed - reset and start over
        if (currentCache.isNotEmpty()) {
            println("♻️ All quotes displayed. Resetting display history...")
            displayedQuoteIds.clear()
            
            val resetQuote = currentCache.random()
            displayedQuoteIds.add(resetQuote.id.toInt())
            println("🔄 Returning reset quote: \"${resetQuote.text.take(50)}...\"")
            return resetQuote
        } else {
            println("⚠️ No quotes available, returning emergency quote")
            return emergencyQuote
        }
    }
    
    /**
     * Get all cached quotes
     */
    fun getAllQuotes(): List<Quote> {
        return _cachedQuotes.value
    }
    
    /**
     * Get quotes by their IDs (for favorites display)
     */
    fun getQuotesByIds(ids: Set<String>): List<Quote> {
        return _cachedQuotes.value.filter { quote ->
            ids.contains(quote.id)
        }
    }
    
    /**
     * Refresh quote cache from API - uses Quotable.io's generous rate limits
     */
    suspend fun refreshQuotes() {
        _isLoading.value = true
        _error.value = null
        
        try {
            println("Starting quote refresh...") // Debug logging
            // Fetch more quotes since Quotable.io has generous rate limits (180/minute)
            val result = quotableApi.getMultipleRandomQuotes(25) // Much larger batch
            if (result.isSuccess) {
                val quotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
                println("✅ Successfully fetched ${quotes.size} quotes from API")
                cacheMutex.withLock {
                    // Merge new quotes with existing cache, maintaining uniqueness
                    val currentQuotes = _cachedQuotes.value.toMutableList()
                    quotes.forEach { newQuote ->
                        if (currentQuotes.none { it.id == newQuote.id }) {
                            currentQuotes.add(newQuote)
                        }
                    }
                    _cachedQuotes.value = currentQuotes
                    println("📦 Cache updated with ${_cachedQuotes.value.size} total quotes")
                    println("📊 ${displayedQuoteIds.size} quotes have been displayed")
                }
                _error.value = null // Clear any previous errors
            } else {
                val exception = result.exceptionOrNull()
                val errorMsg = exception?.message ?: "Unable to fetch quotes"
                println("Quote refresh failed: $errorMsg") // Debug logging
                _error.value = "Network error: $errorMsg"
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
     * Refill cache when running low - uses Quotable.io's generous rate limits
     */
    private suspend fun refillCache() {
        if (_isLoading.value) return // Already loading
        
        _isLoading.value = true
        
        try {
            // Fetch more quotes since Quotable.io has generous rate limits
            val quotesToFetch = 15 // Reasonable batch size for background refill
            println("Background cache refill: attempting to fetch $quotesToFetch quotes...")
            
            val result = quotableApi.getMultipleRandomQuotes(quotesToFetch)
            if (result.isSuccess) {
                val newQuotes = result.getOrNull()?.map { it.toQuote() } ?: emptyList()
                println("✅ Background refill: fetched ${newQuotes.size} new quotes") // Debug logging
                cacheMutex.withLock {
                    val currentQuotes = _cachedQuotes.value.toMutableList()
                    newQuotes.forEach { newQuote ->
                        if (currentQuotes.none { it.id == newQuote.id }) {
                            currentQuotes.add(newQuote)
                        }
                    }
                    _cachedQuotes.value = currentQuotes
                    println("📦 Background refill complete. Cache size: ${_cachedQuotes.value.size}")
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                println("⚠️ Background refill failed: $error")
            }
        } catch (e: Exception) {
            println("❌ Background refill exception: ${e.message}") // Debug logging
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
        id = this.id.toString()
    )
}