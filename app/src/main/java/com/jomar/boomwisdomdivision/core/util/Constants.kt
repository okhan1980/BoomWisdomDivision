package com.jomar.boomwisdomdivision.core.util

/**
 * Application-wide constants for the Boom Wisdom Division app.
 * 
 * This object contains all the constant values used throughout the application,
 * providing a centralized location for configuration and avoiding magic numbers/strings.
 */
object Constants {
    
    // API Configuration
    /**
     * Base URL for the Quotable API service.
     * This is the primary source for fetching random motivational quotes.
     */
    const val BASE_URL = "https://api.quotable.io/"
    
    /**
     * API endpoint for fetching random quotes.
     */
    const val RANDOM_QUOTE_ENDPOINT = "random"
    
    /**
     * Network timeout duration in seconds for API requests.
     */
    const val NETWORK_TIMEOUT_SECONDS = 30L
    
    /**
     * Maximum number of retry attempts for failed API requests.
     */
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Database Configuration
    /**
     * Name of the local SQLite database file.
     */
    const val DATABASE_NAME = "boom_wisdom_database"
    
    /**
     * Current database schema version.
     */
    const val DATABASE_VERSION = 1
    
    /**
     * Name of the quotes table in the database.
     */
    const val QUOTES_TABLE_NAME = "quotes"
    
    // UI Configuration
    /**
     * Duration in milliseconds for the quote transition animation.
     * This matches the design specification of 1.5 seconds total.
     */
    const val QUOTE_TRANSITION_DURATION_MS = 1500L
    
    /**
     * Duration in milliseconds for the brightness overlay fade-in phase.
     */
    const val BRIGHTNESS_FADE_IN_DURATION_MS = 500L
    
    /**
     * Duration in milliseconds for the text fade-out phase.
     */
    const val TEXT_FADE_OUT_DURATION_MS = 300L
    
    /**
     * Duration in milliseconds for the text fade-in phase.
     */
    const val TEXT_FADE_IN_DURATION_MS = 300L
    
    /**
     * Duration in milliseconds for the brightness overlay fade-out phase.
     */
    const val BRIGHTNESS_FADE_OUT_DURATION_MS = 500L
    
    /**
     * Maximum brightness overlay alpha value (0.0 to 1.0).
     */
    const val MAX_BRIGHTNESS_ALPHA = 0.6f
    
    /**
     * Scale factor for the star button press animation.
     */
    const val STAR_BUTTON_PRESS_SCALE = 0.95f
    
    /**
     * Duration in milliseconds for the bookmark animation.
     */
    const val BOOKMARK_ANIMATION_DURATION_MS = 300L
    
    /**
     * Maximum scale for the bookmark bounce effect.
     */
    const val BOOKMARK_BOUNCE_SCALE = 1.2f
    
    // Content Configuration
    /**
     * Minimum quote length to display (in characters).
     * Very short quotes might not provide meaningful content.
     */
    const val MIN_QUOTE_LENGTH = 10
    
    /**
     * Maximum quote length to display (in characters).
     * Very long quotes might not fit well in the UI.
     */
    const val MAX_QUOTE_LENGTH = 500
    
    /**
     * Maximum number of quotes to keep in local favorites.
     * This prevents unlimited storage growth.
     */
    const val MAX_SAVED_QUOTES = 1000
    
    /**
     * Default author text when no author is provided.
     */
    const val UNKNOWN_AUTHOR = "Unknown"
    
    // Error Messages
    /**
     * Generic network error message for user display.
     */
    const val ERROR_NETWORK = "Unable to connect to the server. Please check your internet connection."
    
    /**
     * Error message when no quotes are available.
     */
    const val ERROR_NO_QUOTES = "No quotes available at the moment. Please try again later."
    
    /**
     * Error message when quote saving fails.
     */
    const val ERROR_SAVE_QUOTE = "Failed to save quote to favorites. Please try again."
    
    /**
     * Error message when quote deletion fails.
     */
    const val ERROR_DELETE_QUOTE = "Failed to remove quote from favorites. Please try again."
    
    /**
     * Generic database error message.
     */
    const val ERROR_DATABASE = "A database error occurred. Please restart the app."
    
    /**
     * Error message when a quote is already saved.
     */
    const val ERROR_QUOTE_ALREADY_SAVED = "This quote is already in your favorites."
    
    // Logging Tags
    /**
     * Log tag for network operations.
     */
    const val LOG_TAG_NETWORK = "BoomWisdom_Network"
    
    /**
     * Log tag for database operations.
     */
    const val LOG_TAG_DATABASE = "BoomWisdom_Database"
    
    /**
     * Log tag for repository operations.
     */
    const val LOG_TAG_REPOSITORY = "BoomWisdom_Repository"
    
    /**
     * Log tag for use case operations.
     */
    const val LOG_TAG_USE_CASE = "BoomWisdom_UseCase"
    
    /**
     * Log tag for UI operations.
     */
    const val LOG_TAG_UI = "BoomWisdom_UI"
    
    // Preferences Keys
    /**
     * SharedPreferences key for storing the last viewed quote ID.
     */
    const val PREF_LAST_QUOTE_ID = "last_quote_id"
    
    /**
     * SharedPreferences key for storing the app's first launch flag.
     */
    const val PREF_FIRST_LAUNCH = "first_launch"
    
    /**
     * SharedPreferences key for storing user preferences.
     */
    const val PREF_USER_PREFERENCES = "user_preferences"
    
    // Intent Extra Keys
    /**
     * Intent extra key for passing quote data between activities.
     */
    const val EXTRA_QUOTE = "extra_quote"
    
    /**
     * Intent extra key for passing quote ID.
     */
    const val EXTRA_QUOTE_ID = "extra_quote_id"
    
    // Notification Configuration
    /**
     * Notification channel ID for daily quote notifications (future feature).
     */
    const val NOTIFICATION_CHANNEL_ID = "daily_quotes"
    
    /**
     * Notification channel name for daily quote notifications (future feature).
     */
    const val NOTIFICATION_CHANNEL_NAME = "Daily Quotes"
}