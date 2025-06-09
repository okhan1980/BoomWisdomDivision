package com.jomar.boomwisdomdivision.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jomar.boomwisdomdivision.R
import com.jomar.boomwisdomdivision.model.Quote
import com.jomar.boomwisdomdivision.model.AppState
import com.jomar.boomwisdomdivision.ui.theme.*

@Composable
fun CRTMonitor(
    quote: Quote,
    currentAppState: AppState = AppState.MOTIVATION,
    isFavorite: Boolean = false,
    isTransitioning: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onNextQuote: () -> Unit = {},
    onPreviousQuote: () -> Unit = {},
    onAppStateChange: (AppState) -> Unit = {},
    onViewFavorites: () -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Star button scale animation
    var isStarPressed by remember { mutableStateOf(false) }
    val starScale by animateFloatAsState(
        targetValue = if (isStarPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "star_scale"
    )
    
    // Glow animation for transitions
    val glowAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0.3f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "glow_alpha"
    )
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Full-screen backdrop using the design image
        Image(
            painter = painterResource(id = R.drawable.boom_wisdom_backdrop),
            contentDescription = "CRT Monitor Backdrop",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Quote text overlay positioned to completely fill CRT screen area
        // Move CRT screen 25dp right and 2dp down (CORRECTED)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 262.dp, start = 72.dp, end = 48.dp), // Moved 25dp right (47+25=72, 73-25=48) and 2dp down (260+2=262)
            contentAlignment = Alignment.TopCenter
        ) {
            // White canvas overlay COMPLETELY filling CRT screen from background image
            Box(
                modifier = Modifier
                    .size(width = 275.dp, height = 270.dp) // Reduced width by 5dp (280-5=275)
                    .background(
                        color = Color(0xFFF8F8F8), // Exact screen color from design
                        shape = RoundedCornerShape(6.dp) // Sharper corners to match background screen
                    )
            )
            
            // Glow overlay for transitions
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(width = 275.dp, height = 270.dp)
                        .background(
                            color = CRTGoldenGlow.copy(alpha = glowAlpha),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }
            
            Box(
                modifier = Modifier
                    .size(width = 275.dp, height = 270.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Tap left side to go to previous quote
                                // Tap right side to go to next quote
                                if (offset.x < size.width / 2) {
                                    onPreviousQuote()
                                } else {
                                    onNextQuote()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        // Loading indicator
                        Text(
                            text = "LOADING WISDOM...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2A2A2A),
                                letterSpacing = 2.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )
                    }
                    else -> {
                        // Quote text with perspective transformation to simulate angled CRT screen
                        Text(
                            text = quote.text.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.SansSerif, // Clean sans-serif like in design
                                fontSize = 17.sp, // Increased for larger screen area
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2A2A2A), // Dark gray text
                                lineHeight = 22.sp,
                                letterSpacing = 1.2.sp // Optimized letter spacing
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .graphicsLayer {
                                    // Apply perspective transformation to simulate angled viewing
                                    rotationX = -5f // Slight upward tilt to simulate CRT angle
                                    scaleY = 0.95f // Slightly compress vertically for perspective
                                }
                        )
                    }
                }
            }
        }
        
        // Author attribution positioned like in design (bottom right of moved CRT screen)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 492.dp, end = 90.dp), // Moved 10dp right (100-10=90)
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .background(
                        CRTGlow,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(10.dp, 5.dp)
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        
        // Interactive buttons positioned to perfectly overlap background image buttons
        // Star button - moved 5dp more right and made 2dp taller (1dp up, 1dp down)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 125.dp, y = (-36).dp) // Moved 5dp more right (120+5=125) and 1dp up (-35-1=-36)
                .size(width = 150.dp, height = 77.dp) // Made 2dp taller (75+2=77)
                .graphicsLayer {
                    scaleX = starScale
                    scaleY = starScale
                }
                .background(
                    CRTGlow,
                    RoundedCornerShape(37.dp)
                )
                .clickable { 
                    isStarPressed = true
                    onFavoriteClick()
                    onNextQuote() // Also change to next quote when star is pressed
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = Color.Black,
                modifier = Modifier.size(38.dp)
            )
        }
        
        // Reset star press state after animation
        LaunchedEffect(isStarPressed) {
            if (isStarPressed) {
                delay(100)
                isStarPressed = false
            }
        }
        
        // Favorites list button - positioned to exactly cover background bookmark button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = (-60).dp)
                .size(60.dp)
                .background(
                    Color.Transparent,
                    CircleShape
                )
                .clickable { onViewFavorites() },
            contentAlignment = Alignment.Center
        ) {
            // Border ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Transparent,
                        CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .background(
                            Color.Transparent,
                            CircleShape
                        )
                )
            }
            
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "View favorites",
                tint = CRTGlow,
                modifier = Modifier.size(30.dp)
            )
        }
        
        // Interactive text buttons positioned over background image text
        // MOTIVATION button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 45.dp, y = 75.dp)
                .clickable { onAppStateChange(AppState.MOTIVATION) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MOTIVATION",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentAppState == AppState.MOTIVATION) CRTGoldenGlow else Color(0xFF8B4513),
                    letterSpacing = 1.sp
                )
            )
        }
        
        // MINDFULNESS button - positioned over background text  
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 0.dp, y = 75.dp)
                .clickable { onAppStateChange(AppState.MINDFULNESS) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MINDFULNESS",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentAppState == AppState.MINDFULNESS) CRTGoldenGlow else Color(0xFF8B4513),
                    letterSpacing = 1.sp
                )
            )
        }
        
        // CREATIVITY button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-45).dp, y = 75.dp)
                .clickable { onAppStateChange(AppState.CREATIVITY) }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CREATIVITY",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentAppState == AppState.CREATIVITY) CRTGoldenGlow else Color(0xFF8B4513),
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CRTMonitorPreview() {
    BoomWisdomDivisionTheme {
        CRTMonitor(
            quote = Quote(
                text = "The future belongs to those who believe in the beauty of their dreams",
                author = "Eleanor Roosevelt"
            ),
            currentAppState = AppState.MOTIVATION,
            isFavorite = false,
            isTransitioning = false,
            onAppStateChange = {},
            onViewFavorites = {},
            isLoading = false,
            error = null
        )
    }
}