package com.jomar.boomwisdomdivision.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.jomar.boomwisdomdivision.ui.theme.*

@Composable
fun CRTMonitor(
    quote: Quote,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onNextQuote: () -> Unit = {},
    onPreviousQuote: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                .background(
                    CRTGlow,
                    RoundedCornerShape(37.dp)
                )
                .clickable { 
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
        
        // Bookmark button - positioned to exactly cover background bookmark button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-50).dp, y = (-60).dp)
                .size(60.dp)
                .background(
                    Color.Transparent,
                    CircleShape
                )
                .clickable { /* TODO: Implement bookmark */ },
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
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Bookmark",
                tint = CRTGlow,
                modifier = Modifier.size(30.dp)
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
            isFavorite = false
        )
    }
}