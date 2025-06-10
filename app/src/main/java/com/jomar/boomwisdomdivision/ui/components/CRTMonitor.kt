package com.jomar.boomwisdomdivision.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ripple
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
        targetValue = if (currentAppState == AppState.MOTIVATION) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "motivation_alpha"
    )
    val mindfulnessAlpha by animateFloatAsState(
        targetValue = if (currentAppState == AppState.MINDFULNESS) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "mindfulness_alpha"
    )
    val creativityAlpha by animateFloatAsState(
        targetValue = if (currentAppState == AppState.CREATIVITY) 1f else 0.6f,
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
            AppState.MOTIVATION -> R.drawable.wisdom_inspiration_bg
            AppState.CREATIVITY -> R.drawable.wisdom_creativity_bg
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
        // Use same dimensions for all states to maintain consistent positioning
        val screenHeight = if (currentAppState == AppState.CREATIVITY || currentAppState == AppState.MOTIVATION) 215.dp else 275.dp
        val screenWidth = 282.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 314.dp, start = 63.dp, end = 53.dp), // Moved down 40dp total (274 + 40 = 314)
            contentAlignment = Alignment.TopCenter
        ) {
            // Removed white background overlay - keeping only text
            
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
                            // Use fixed font size as requested
                            val baseText = animatedQuote.text.uppercase()
                            val fontSize = 26.sp
                            val lineHeight = 31.sp
                            
                            // Dynamic text color based on app state
                            val textColor = if (currentAppState == AppState.CREATIVITY) {
                                Color(0xFFDED1CF) // Light beige for creativity
                            } else {
                                Color(0xFF2A2A2A) // Dark gray for motivation/mindfulness
                            }
                            
                            // Quote text with perspective transformation to simulate angled CRT screen
                            Text(
                                text = baseText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = InterFontFamily, // Inter font from Google Fonts
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Light,
                                    color = textColor,
                                    lineHeight = lineHeight,
                                    letterSpacing = 0.8.sp
                                ),
                                textAlign = TextAlign.Left,
                                maxLines = 8, // Limit to prevent overflow into author area
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Removed author attribution and yellow rectangle
        
        // Interactive buttons positioned to perfectly overlap background image buttons
        // Star button - using custom button image
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 125.dp, y = (-41).dp) // Moved up 5dp more (-36-5=-41)
                .size(width = 172.dp, height = 88.dp) // 12% smaller (195*0.88=172, 100*0.88=88)
                .graphicsLayer {
                    scaleX = starScale
                    scaleY = starScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        radius = 46.dp
                    )
                ) { 
                    isStarPressed = true
                    onFavoriteClick()
                    onNextQuote() // Also change to next quote when star is pressed
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.button_wisdom),
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        // Reset star press state after animation
        LaunchedEffect(isStarPressed) {
            if (isStarPressed) {
                delay(100)
                isStarPressed = false
            }
        }
        
        // Favorites list button - using custom button image
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = (-60).dp)
                .size(51.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        radius = 26.dp
                    )
                ) { onViewFavorites() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.button_saved),
                contentDescription = "View favorites",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        // Interactive text buttons positioned over background image text - Tab bar behavior
        // MOTIVATION tab button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 45.dp, y = 145.dp)
                .graphicsLayer {
                    scaleX = motivationScale
                    scaleY = motivationScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        radius = 50.dp
                    )
                ) {
                    if (currentAppState != AppState.MOTIVATION) {
                        onAppStateChange(AppState.MOTIVATION)
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MOTIVATION",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.MOTIVATION) FontWeight.Medium else FontWeight.Normal,
                    color = tabTextColor.copy(alpha = motivationAlpha),
                    letterSpacing = 0.5.sp
                )
            )
        }
        
        // MINDFULNESS tab button - positioned over background text  
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 0.dp, y = 145.dp)
                .graphicsLayer {
                    scaleX = mindfulnessScale
                    scaleY = mindfulnessScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        radius = 50.dp
                    )
                ) {
                    if (currentAppState != AppState.MINDFULNESS) {
                        onAppStateChange(AppState.MINDFULNESS)
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MINDFULNESS",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.MINDFULNESS) FontWeight.Medium else FontWeight.Normal,
                    color = tabTextColor.copy(alpha = mindfulnessAlpha),
                    letterSpacing = 0.5.sp
                )
            )
        }
        
        // CREATIVITY tab button - positioned over background text
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-45).dp, y = 145.dp)
                .graphicsLayer {
                    scaleX = creativityScale
                    scaleY = creativityScale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(
                        bounded = true,
                        radius = 50.dp
                    )
                ) {
                    if (currentAppState != AppState.CREATIVITY) {
                        onAppStateChange(AppState.CREATIVITY)
                    }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CREATIVITY",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Default,
                    fontSize = 11.sp,
                    fontWeight = if (currentAppState == AppState.CREATIVITY) FontWeight.Medium else FontWeight.Normal,
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