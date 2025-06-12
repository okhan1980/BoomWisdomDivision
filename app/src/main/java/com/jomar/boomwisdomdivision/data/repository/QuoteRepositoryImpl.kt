package com.jomar.boomwisdomdivision.data.repository

import com.jomar.boomwisdomdivision.data.api.ClaudeApi
import com.jomar.boomwisdomdivision.model.Quote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Simple quote repository implementation without DI framework
 * Manages quote generation using Claude API and caching
 */
class QuoteRepositoryImpl {
    
    private val claudeApi = ClaudeApi.getInstance()
    private val cacheMutex = Mutex()
    
    // Current selected category
    private var currentCategory = "motivation"
    
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
     * Get a generated quote from Claude API based on selected category
     */
    suspend fun getRandomQuote(category: String = currentCategory): Quote {
        println("=== QuoteRepositoryImpl.getRandomQuote() called ===")
        println("📊 Displayed quotes so far: ${displayedQuoteIds.size}")
        
        // Update current category
        currentCategory = category
        
        // Try Claude API first
        try {
            val apiQuote = claudeApi.generateQuote(category)
            
            if (apiQuote != null) {
                    // Claude generates unique quotes, so no need to check for duplicates
                    println("✅ Claude API Success! Generated quote: \"${apiQuote.text}\" - ${apiQuote.author}")
                    
                    // Track for consistency (though not needed for uniqueness)
                    displayedQuoteIds.add(apiQuote.id.hashCode())
                        
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
                println("❌ Claude API returned null")
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
     * Refresh quote cache from Claude API - generates multiple quotes
     */
    suspend fun refreshQuotes() {
        _isLoading.value = true
        _error.value = null
        
        try {
            println("Starting quote generation...") // Debug logging
            // Generate quotes for each category to build diverse cache
            val categories = listOf("motivation", "mindfulness", "creativity")
            val quotes = mutableListOf<Quote>()
            
            // Generate 3 quotes per category (9 total)
            for (category in categories) {
                for (i in 1..3) {
                    val quote = claudeApi.generateQuote(category)
                    if (quote != null) {
                        quotes.add(quote)
                        delay(500) // Small delay to avoid rate limits
                    }
                }
            }
            
            println("✅ Successfully generated ${quotes.size} quotes from Claude")
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
        } catch (e: Exception) {
            val errorMsg = "Unexpected error: ${e.message ?: "Unknown error occurred"}"
            println("Quote refresh exception: $errorMsg") // Debug logging
            _error.value = errorMsg
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Refill cache when running low - generates quotes from Claude
     */
    private suspend fun refillCache() {
        if (_isLoading.value) return // Already loading
        
        _isLoading.value = true
        
        try {
            val categories = listOf("motivation", "mindfulness", "creativity")
            val newQuotes = mutableListOf<Quote>()
            println("Background cache refill: generating new quotes...")
            
            // Generate 2 quotes per category (6 total) for background refill
            for (category in categories) {
                for (i in 1..2) {
                    val quote = claudeApi.generateQuote(category)
                    if (quote != null) {
                        newQuotes.add(quote)
                        delay(500) // Small delay
                    }
                }
            }
            
            println("✅ Background refill: generated ${newQuotes.size} new quotes") // Debug logging
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
    
    /**
     * Set the current category for quote generation
     */
    fun setCategory(category: String) {
        currentCategory = category
    }
    
    /**
     * Get the current selected category
     */
    fun getCurrentCategory(): String = currentCategory
}