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
import com.jomar.boomwisdomdivision.model.Quote
import com.jomar.boomwisdomdivision.model.QuoteRepository
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
    var currentQuoteIndex by remember { mutableStateOf(0) }
    var favorites by remember { mutableStateOf(setOf<Int>()) }
    
    val currentQuote = QuoteRepository.getQuoteByIndex(currentQuoteIndex)
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CRTBackground
    ) {
        // Full-screen CRT Monitor with backdrop
        AnimatedContent(
            targetState = currentQuote,
            transitionSpec = {
                fadeIn(animationSpec = tween(750)) togetherWith 
                fadeOut(animationSpec = tween(750))
            },
            label = "quote_transition"
        ) { quote ->
            CRTMonitor(
                quote = quote,
                isFavorite = favorites.contains(quote.id),
                onFavoriteClick = {
                    val isFav = favorites.contains(quote.id)
                    favorites = if (isFav) {
                        favorites - quote.id
                    } else {
                        favorites + quote.id
                    }
                },
                onNextQuote = {
                    currentQuoteIndex = (currentQuoteIndex + 1) % QuoteRepository.getQuoteCount()
                },
                onPreviousQuote = {
                    currentQuoteIndex = if (currentQuoteIndex > 0) {
                        currentQuoteIndex - 1
                    } else {
                        QuoteRepository.getQuoteCount() - 1
                    }
                }
            )
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
