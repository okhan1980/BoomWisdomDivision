package com.jomar.boomwisdomdivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jomar.boomwisdomdivision.model.Quote
import com.jomar.boomwisdomdivision.model.AppState
import com.jomar.boomwisdomdivision.data.repository.QuoteRepositoryImpl
import com.jomar.boomwisdomdivision.data.preferences.PreferencesManager
import com.jomar.boomwisdomdivision.ui.components.CRTMonitor
import com.jomar.boomwisdomdivision.ui.screens.FavoritesScreen
import com.jomar.boomwisdomdivision.ui.theme.BoomWisdomDivisionTheme
import com.jomar.boomwisdomdivision.ui.theme.CRTBackground
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoomWisdomDivisionTheme {
                BoomWisdomApp()
            }
        }
    }
}

@Composable
fun BoomWisdomApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val quoteRepository = remember { QuoteRepositoryImpl.getInstance() }
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    var currentQuote by remember { mutableStateOf<Quote?>(null) }
    var currentAppState by remember { mutableStateOf(AppState.MOTIVATION) }
    val scope = rememberCoroutineScope()
    
    // Observe favorites from preferences
    val favoriteIds by preferencesManager.favoriteQuotes.collectAsStateWithLifecycle()
    
    // Observe loading state and errors
    val isLoading by quoteRepository.isLoading.collectAsStateWithLifecycle()
    val error by quoteRepository.error.collectAsStateWithLifecycle()
    val cachedQuotes by quoteRepository.cachedQuotes.collectAsStateWithLifecycle()
    
    // Load initial quote and app state
    LaunchedEffect(Unit) {
        // Initialize app state to MOTIVATION (ensure it's always the default)
        currentAppState = AppState.MOTIVATION
        preferencesManager.setAppState(AppState.MOTIVATION)
        
        // Check if we have a last viewed quote
        val lastViewed = preferencesManager.getLastViewedQuote()
        currentQuote = lastViewed ?: quoteRepository.getRandomQuote(currentAppState.displayName.lowercase())
        
        // Trigger initial cache refresh in background
        try {
            quoteRepository.refreshQuotes()
        } catch (e: Exception) {
            println("Background refresh failed: ${e.message}")
        }
    }
    
    // Save current quote when it changes
    LaunchedEffect(currentQuote) {
        currentQuote?.let { quote ->
            preferencesManager.saveLastViewedQuote(quote)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CRTBackground
    ) {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                currentQuote?.let { quote ->
                    // Fixed CRT Monitor - only the quote content will animate
                    CRTMonitor(
                        quote = quote,
                        currentAppState = currentAppState,
                        isFavorite = favoriteIds.contains(quote.id),
                        isTransitioning = false, // No longer needed
                        onFavoriteClick = {
                            preferencesManager.toggleFavorite(quote.id)
                        },
                        onAppStateChange = { newState ->
                            // Tab behavior: only change if different from current state
                            if (currentAppState != newState) {
                                currentAppState = newState
                                preferencesManager.setAppState(newState)
                                
                                // Fetch new quote when switching tabs
                                scope.launch {
                                    println("Tab switched to: ${newState.displayName}")
                                    // Get quote for the selected category
                                    currentQuote = quoteRepository.getRandomQuote(newState.displayName.lowercase())
                                }
                            }
                        },
                        onNextQuote = {
                            scope.launch {
                                println("User requested next quote") // Debug
                                currentQuote = quoteRepository.getRandomQuote(currentAppState.displayName.lowercase())
                            }
                        },
                        onPreviousQuote = {
                            scope.launch {
                                println("User requested previous quote") // Debug  
                                currentQuote = quoteRepository.getRandomQuote(currentAppState.displayName.lowercase())
                            }
                        },
                        onViewFavorites = {
                            navController.navigate("favorites")
                        },
                        isLoading = isLoading,
                        error = error,
                        onRetry = {
                            scope.launch {
                                quoteRepository.clearError()
                                quoteRepository.refreshQuotes()
                            }
                        }
                    )
                }
            }
            
            composable("favorites") {
                val favoriteQuotes = quoteRepository.getQuotesByIds(favoriteIds)
                FavoritesScreen(
                    favoriteQuotes = favoriteQuotes,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onQuoteClick = { quote ->
                        currentQuote = quote
                        navController.popBackStack()
                    },
                    onToggleFavorite = { quote ->
                        preferencesManager.toggleFavorite(quote.id)
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BoomWisdomAppPreview() {
    BoomWisdomDivisionTheme {
        BoomWisdomApp()
    }
}
