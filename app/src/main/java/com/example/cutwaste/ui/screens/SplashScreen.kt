package com.example.cutwaste.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val barWidthRatio = remember { Animatable(0f) }
    val cutsAlpha = remember { Animatable(0f) }
    val leftoverOffset = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val wasteBadgeAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animation timeline (total ~1.3 seconds)
        barWidthRatio.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        cutsAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300)
        )
        leftoverOffset.animateTo(
            targetValue = 12f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        wasteBadgeAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200)
        )
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 250)
        )
        delay(200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated Material Stock Bar
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(36.dp)
            ) {
                // Background bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barWidthRatio.value)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Consumed cut segment 1
                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .height(36.dp)
                                .alpha(cutsAlpha.value)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        // Kerf gap
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(36.dp)
                                .alpha(cutsAlpha.value)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        // Consumed cut segment 2
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .height(36.dp)
                                .alpha(cutsAlpha.value)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                        )
                        // Kerf gap
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(36.dp)
                                .alpha(cutsAlpha.value)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        // Remaining material separating slightly
                        Box(
                            modifier = Modifier
                                .weight(0.25f)
                                .height(36.dp)
                                .padding(start = (leftoverOffset.value * 0.5f).dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                    }
                }
            }

            // Waste Percentage Pill
            Box(
                modifier = Modifier
                    .alpha(wasteBadgeAlpha.value)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "REMAINING: 494 mm  •  WASTE: 16.5%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Title and Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = "CUTWASTE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Calculate leftover material after cuts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
