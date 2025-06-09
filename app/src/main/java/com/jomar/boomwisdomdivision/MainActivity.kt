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
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
    var currentAppState by remember { mutableStateOf(preferencesManager.getAppState()) }
    val scope = rememberCoroutineScope()
    
    // Observe favorites from preferences
    val favoriteIds by preferencesManager.favoriteQuotes.collectAsStateWithLifecycle()
    
    // Observe loading state and errors
    val isLoading by quoteRepository.isLoading.collectAsStateWithLifecycle()
    val error by quoteRepository.error.collectAsStateWithLifecycle()
    val cachedQuotes by quoteRepository.cachedQuotes.collectAsStateWithLifecycle()
    
    // Load initial quote
    LaunchedEffect(Unit) {
        // Check if we have a last viewed quote
        val lastViewed = preferencesManager.getLastViewedQuote()
        currentQuote = lastViewed ?: quoteRepository.getRandomQuote()
        
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
                    // Add a glow state for quote transitions
                    var isTransitioning by remember { mutableStateOf(false) }
                    
                    // Trigger glow animation on quote change
                    LaunchedEffect(quote) {
                        isTransitioning = true
                        delay(1500) // Total animation time
                        isTransitioning = false
                    }
                    
                    // Full-screen CRT Monitor with backdrop
                    AnimatedContent(
                        targetState = quote,
                        transitionSpec = {
                            // Custom transition: glow -> fade out -> fade in -> unglow
                            val fadeOutDuration = 500
                            val fadeInDuration = 500
                            val glowDuration = 250
                            
                            fadeIn(
                                animationSpec = tween(
                                    durationMillis = fadeInDuration,
                                    delayMillis = fadeOutDuration + glowDuration
                                )
                            ) togetherWith fadeOut(
                                animationSpec = tween(
                                    durationMillis = fadeOutDuration,
                                    delayMillis = glowDuration
                                )
                            )
                        },
                        label = "quote_transition"
                    ) { animatedQuote ->
                        CRTMonitor(
                            quote = animatedQuote,
                            currentAppState = currentAppState,
                            isFavorite = favoriteIds.contains(animatedQuote.id),
                            isTransitioning = isTransitioning,
                            onFavoriteClick = {
                                preferencesManager.toggleFavorite(animatedQuote.id)
                            },
                            onAppStateChange = { newState ->
                                // Tab behavior: only change if different from current state
                                if (currentAppState != newState) {
                                    currentAppState = newState
                                    preferencesManager.setAppState(newState)
                                    
                                    // Fetch new quote when switching tabs
                                    scope.launch {
                                        println("Tab switched to: ${newState.displayName}")
                                        // TODO: Implement category-specific filtering based on app state
                                        currentQuote = quoteRepository.getRandomQuote()
                                    }
                                }
                            },
                            onNextQuote = {
                                scope.launch {
                                    println("User requested next quote") // Debug
                                    currentQuote = quoteRepository.getRandomQuote()
                                }
                            },
                            onPreviousQuote = {
                                scope.launch {
                                    println("User requested previous quote") // Debug  
                                    currentQuote = quoteRepository.getRandomQuote()
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
