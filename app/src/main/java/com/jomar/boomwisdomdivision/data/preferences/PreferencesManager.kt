package com.jomar.boomwisdomdivision.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.jomar.boomwisdomdivision.model.Quote
import com.jomar.boomwisdomdivision.model.AppState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages app preferences using SharedPreferences
 * Handles favorites, last viewed quote, and user settings
 */
class PreferencesManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // StateFlow for reactive favorites updates
    private val _favoriteQuotes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteQuotes: StateFlow<Set<String>> = _favoriteQuotes.asStateFlow()
    
    init {
        // Load favorites on initialization
        _favoriteQuotes.value = loadFavoriteIds()
    }
    
    companion object {
        private const val PREFS_NAME = "boom_wisdom_prefs"
        private const val KEY_FAVORITE_IDS = "favorite_quote_ids"
        private const val KEY_LAST_VIEWED_QUOTE = "last_viewed_quote"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_STATE = "app_state"
        
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Add a quote to favorites
     */
    fun addFavorite(quoteId: String) {
        val currentFavorites = _favoriteQuotes.value.toMutableSet()
        currentFavorites.add(quoteId)
        saveFavoriteIds(currentFavorites)
        _favoriteQuotes.value = currentFavorites
    }
    
    /**
     * Remove a quote from favorites
     */
    fun removeFavorite(quoteId: String) {
        val currentFavorites = _favoriteQuotes.value.toMutableSet()
        currentFavorites.remove(quoteId)
        saveFavoriteIds(currentFavorites)
        _favoriteQuotes.value = currentFavorites
    }
    
    /**
     * Toggle favorite status for a quote
     */
    fun toggleFavorite(quoteId: String): Boolean {
        val isFavorite = isFavorite(quoteId)
        if (isFavorite) {
            removeFavorite(quoteId)
        } else {
            addFavorite(quoteId)
        }
        return !isFavorite
    }
    
    /**
     * Check if a quote is marked as favorite
     */
    fun isFavorite(quoteId: String): Boolean {
        return _favoriteQuotes.value.contains(quoteId)
    }
    
    /**
     * Get all favorite quote IDs
     */
    fun getFavoriteIds(): Set<String> {
        return _favoriteQuotes.value
    }
    
    /**
     * Save the last viewed quote
     */
    fun saveLastViewedQuote(quote: Quote) {
        val adapter = moshi.adapter(Quote::class.java)
        val json = adapter.toJson(quote)
        prefs.edit().putString(KEY_LAST_VIEWED_QUOTE, json).apply()
    }
    
    /**
     * Get the last viewed quote
     */
    fun getLastViewedQuote(): Quote? {
        val json = prefs.getString(KEY_LAST_VIEWED_QUOTE, null) ?: return null
        return try {
            val adapter = moshi.adapter(Quote::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if this is the first app launch
     */
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * Mark that the app has been launched
     */
    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    /**
     * Get theme mode preference (for future dark mode support)
     */
    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    }
    
    /**
     * Set theme mode preference
     */
    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }
    
    /**
     * Get current app state
     */
    fun getAppState(): AppState {
        val stateName = prefs.getString(KEY_APP_STATE, AppState.MOTIVATION.name) ?: AppState.MOTIVATION.name
        return try {
            AppState.valueOf(stateName)
        } catch (e: IllegalArgumentException) {
            AppState.MOTIVATION // Default fallback
        }
    }
    
    /**
     * Set current app state
     */
    fun setAppState(appState: AppState) {
        prefs.edit().putString(KEY_APP_STATE, appState.name).apply()
    }
    
    /**
     * Clear all preferences (for testing or reset)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        _favoriteQuotes.value = emptySet()
    }
    
    // Private helper methods
    
    private fun loadFavoriteIds(): Set<String> {
        val idsString = prefs.getString(KEY_FAVORITE_IDS, "") ?: ""
        return if (idsString.isEmpty()) {
            emptySet()
        } else {
            idsString.split(",").toSet()
        }
    }
    
    private fun saveFavoriteIds(ids: Set<String>) {
        val idsString = ids.joinToString(",")
        prefs.edit().putString(KEY_FAVORITE_IDS, idsString).apply()
    }
}