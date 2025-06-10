package com.jomar.boomwisdomdivision.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    
    // Remove glow animation - not needed
    
    // Tab selection animation with stronger visual contrast
    val motivationAlpha by animateFloatAsState(
        targetValue = if (currentAppState == AppState.MOTIVATION) 1f else 0.3f,
        animationSpec = tween(durationMillis = 200),
        label = "motivation_alpha"
    )
    val mindfulnessAlpha by animateFloatAsState(
        targetValue = if (currentAppState == AppState.MINDFULNESS) 1f else 0.3f,
        animationSpec = tween(durationMillis = 200),
        label = "mindfulness_alpha"
    )
    val creativityAlpha by animateFloatAsState(
        targetValue = if (currentAppState == AppState.CREATIVITY) 1f else 0.3f,
        animationSpec = tween(durationMillis = 200),
        label = "creativity_alpha"
    )
    
    // Add scale animation for selected tabs to make them more prominent
    val motivationScale by animateFloatAsState(
        targetValue = if (currentAppState == AppState.MOTIVATION) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "motivation_scale"
    )
    val mindfulnessScale by animateFloatAsState(
        targetValue = if (currentAppState == AppState.MINDFULNESS) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "mindfulness_scale"
    )
    val creativityScale by animateFloatAsState(
        targetValue = if (currentAppState == AppState.CREATIVITY) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "creativity_scale"
    )
    
    // Dynamic text color based on background - black for Creativity (light bg), white for others (dark bg)
    val tabTextColor = if (currentAppState == AppState.CREATIVITY) Color.Black else Color.White
    
    // Debug: Print current app state
    LaunchedEffect(currentAppState) {
        println("🎯 CRTMonitor currentAppState: ${currentAppState.displayName}")
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Full-screen backdrop using different images based on app state
        val backgroundResource = when (currentAppState) {
            AppState.MOTIVATION -> R.drawable.boom_wisdom_app_bg
            AppState.CREATIVITY -> R.drawable.boom_wisdom_app_creative_bg
            AppState.MINDFULNESS -> R.drawable.boom_wisdom_backdrop // Keep original for mindfulness
        }
        
        AnimatedContent(
            targetState = backgroundResource,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 500)) togetherWith 
                fadeOut(animationSpec = tween(durationMillis = 500))
            },
            label = "background_transition"
        ) { animatedBackground ->
            Image(
                painter = painterResource(id = animatedBackground),
                contentDescription = "CRT Monitor Backdrop",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // Quote text overlay positioned to completely fill CRT screen area
        // Adjust size and position based on app state (different monitor backgrounds)
        val screenHeight = if (currentAppState == AppState.CREATIVITY) 215.dp else 275.dp // 60dp shorter total for Creativity mode
        val screenWidth = 282.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 294.dp, start = 63.dp, end = 53.dp), // Moved down 20dp (274 + 20 = 294)
            contentAlignment = Alignment.TopCenter
        ) {
            // White canvas overlay COMPLETELY filling CRT screen from background image
            Box(
                modifier = Modifier
                    .size(width = screenWidth, height = screenHeight)
                    .background(
                        color = Color(0xFFF8F8F8), // Exact screen color from design
                        shape = RoundedCornerShape(6.dp) // Sharper corners to match background screen
                    )
            )
            
            // Removed glow overlay
            
            Box(
                modifier = Modifier
                    .size(width = screenWidth, height = screenHeight),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = quote,
                    transitionSpec = {
                        // Simple fade transition for quote text only
                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith 
                        fadeOut(animationSpec = tween(durationMillis = 300))
                    },
                    label = "quote_text_transition"
                ) { animatedQuote ->
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
                            // Calculate dynamic font size based on text length
                            val baseText = animatedQuote.text.uppercase()
                            val textLength = baseText.length
                            val dynamicFontSize = when {
                                textLength <= 50 -> 17.sp
                                textLength <= 100 -> 15.sp
                                textLength <= 150 -> 13.sp
                                textLength <= 200 -> 11.sp
                                else -> 10.sp
                            }
                            val dynamicLineHeight = dynamicFontSize * 1.3f
                            
                            // Quote text with perspective transformation to simulate angled CRT screen
                            Text(
                                text = baseText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.SansSerif, // Clean sans-serif like in design
                                    fontSize = dynamicFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2A2A2A), // Dark gray text
                                    lineHeight = dynamicLineHeight,
                                    letterSpacing = 1.2.sp // Optimized letter spacing
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 8, // Limit to prevent overflow into author area
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
        }
        
        // Author attribution positioned like in design (bottom right of moved CRT screen)
        // Adjust position based on screen height for different app states
        val authorTopPadding = if (currentAppState == AppState.CREATIVITY) 469.dp else 529.dp // Moved down 20dp (449+20=469, 509+20=529)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = authorTopPadding, end = 95.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            AnimatedContent(
                targetState = quote.author,
                transitionSpec = {
                    // Simple fade transition for author text only
                    fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith 
                    fadeOut(animationSpec = tween(durationMillis = 300))
                },
                label = "author_text_transition"
            ) { animatedAuthor ->
                Box(
                    modifier = Modifier
                        .background(
                            CRTGlow,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(10.dp, 5.dp)
                ) {
                    Text(
                        text = "— $animatedAuthor",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
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
        
        // Interactive text buttons positioned over background image text - Tab bar behavior
        // MOTIVATION tab button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 45.dp, y = 125.dp)
                .graphicsLayer {
                    scaleX = motivationScale
                    scaleY = motivationScale
                }
                .clickable {
                    if (currentAppState != AppState.MOTIVATION) {
                        onAppStateChange(AppState.MOTIVATION)
                    }
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MOTIVATION",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.MOTIVATION) FontWeight.Medium else FontWeight.Light,
                    color = tabTextColor.copy(alpha = motivationAlpha),
                    letterSpacing = 0.5.sp
                )
            )
        }
        
        // MINDFULNESS tab button - positioned over background text  
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 0.dp, y = 125.dp)
                .graphicsLayer {
                    scaleX = mindfulnessScale
                    scaleY = mindfulnessScale
                }
                .clickable {
                    if (currentAppState != AppState.MINDFULNESS) {
                        onAppStateChange(AppState.MINDFULNESS)
                    }
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MINDFULNESS",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.MINDFULNESS) FontWeight.Medium else FontWeight.Light,
                    color = tabTextColor.copy(alpha = mindfulnessAlpha),
                    letterSpacing = 0.5.sp
                )
            )
        }
        
        // CREATIVITY tab button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-45).dp, y = 125.dp)
                .graphicsLayer {
                    scaleX = creativityScale
                    scaleY = creativityScale
                }
                .clickable {
                    if (currentAppState != AppState.CREATIVITY) {
                        onAppStateChange(AppState.CREATIVITY)
                    }
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CREATIVITY",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.CREATIVITY) FontWeight.Medium else FontWeight.Light,
                    color = tabTextColor.copy(alpha = creativityAlpha),
                    letterSpacing = 0.5.sp
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