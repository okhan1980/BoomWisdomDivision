package com.jomar.boomwisdomdivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.jomar.boomwisdomdivision.model.Quote
import com.jomar.boomwisdomdivision.data.repository.QuoteRepositoryImpl
import com.jomar.boomwisdomdivision.ui.components.CRTMonitor
import com.jomar.boomwisdomdivision.ui.theme.BoomWisdomDivisionTheme
import com.jomar.boomwisdomdivision.ui.theme.CRTBackground

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
    val quoteRepository = remember { QuoteRepositoryImpl.getInstance() }
    var currentQuote by remember { mutableStateOf<Quote?>(null) }
    var favorites by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()
    
    // Observe loading state and errors
    val isLoading by quoteRepository.isLoading.collectAsStateWithLifecycle()
    val error by quoteRepository.error.collectAsStateWithLifecycle()
    val cachedQuotes by quoteRepository.cachedQuotes.collectAsStateWithLifecycle()
    
    // Load initial quote
    LaunchedEffect(Unit) {
        // Always start with a cached/fallback quote for immediate display
        currentQuote = quoteRepository.getRandomQuote()
        // Trigger initial cache refresh in background
        // Don't block the UI for this
        try {
            quoteRepository.refreshQuotes()
        } catch (e: Exception) {
            println("Background refresh failed: ${e.message}")
            // Don't show error immediately - user has quotes to see
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CRTBackground
    ) {
        currentQuote?.let { quote ->
            // Full-screen CRT Monitor with backdrop
            AnimatedContent(
                targetState = quote,
                transitionSpec = {
                    fadeIn(animationSpec = tween(750)) togetherWith 
                    fadeOut(animationSpec = tween(750))
                },
                label = "quote_transition"
            ) { animatedQuote ->
                CRTMonitor(
                    quote = animatedQuote,
                    isFavorite = favorites.contains(animatedQuote.id),
                    onFavoriteClick = {
                        val isFav = favorites.contains(animatedQuote.id)
                        favorites = if (isFav) {
                            favorites - animatedQuote.id
                        } else {
                            favorites + animatedQuote.id
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
}


@Preview(showBackground = true)
@Composable
fun BoomWisdomAppPreview() {
    BoomWisdomDivisionTheme {
        BoomWisdomApp()
    }
}
